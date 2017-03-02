/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidpn.client.SerivceManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.util.Log;


import org.androidpn.client.BuildConfig;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaIdFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.TLSUtils;
import org.jivesoftware.smackx.iqregister.packet.Registration;
import org.jivesoftware.smack.provider.ProviderManager;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.callback.CallbackHandler;

/**
 * This class is to manage the XMPP connection between client and server.
 * 
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class XmppManager {

    private static final String LOGTAG = LogUtil.makeLogTag(XmppManager.class);

    private static final String XMPP_RESOURCE_NAME = "AndroidpnClient";

    private Context context;

    private NotificationService.TaskSubmitter taskSubmitter;

    private NotificationService.TaskTracker taskTracker;

    private SharedPreferences sharedPrefs;

    private String xmppHost;

    private int xmppPort;

    private XMPPTCPConnection connection;

    private String username;

    private String password;

    private ConnectionListener connectionListener;

    private StanzaListener notificationPacketListener;

    private Handler handler;

    private final List<Runnable> taskList;

    private boolean running = false;

    private Future<?> futureTask;

    private Thread reconnection;

    public XmppManager(NotificationService notificationService) {
        context = notificationService;
        taskSubmitter = notificationService.getTaskSubmitter();
        taskTracker = notificationService.getTaskTracker();
        sharedPrefs = notificationService.getSharedPreferences();

        xmppHost = sharedPrefs.getString(Constants.XMPP_HOST, "localhost");
        xmppPort = sharedPrefs.getInt(Constants.XMPP_PORT, 5222);
        username = sharedPrefs.getString(Constants.XMPP_USERNAME, "");
        password = sharedPrefs.getString(Constants.XMPP_PASSWORD, "");

        connectionListener = new PersistentConnectionListener(this);
        notificationPacketListener = new NotificationPacketListener(this);

        handler = new Handler();
        taskList = new ArrayList<Runnable>();
        reconnection = new ReconnectionThread(this);
    }

    public Context getContext() {
        return context;
    }

    public void connect() {
        Log.d(LOGTAG, "connect()...");
        submitLoginTask();
    }

    public void disconnect() {
        Log.d(LOGTAG, "disconnect()...");
        terminatePersistentConnection();
    }

    public void terminatePersistentConnection() {
        Log.d(LOGTAG, "terminatePersistentConnection()...");
        Runnable runnable = new Runnable() {

            final XmppManager xmppManager = XmppManager.this;

            public void run() {
                if (xmppManager.isConnected()) {
                    Log.d(LOGTAG, "terminatePersistentConnection()... run()");
                    xmppManager.getConnection().removeAsyncStanzaListener(
                            xmppManager.getNotificationPacketListener());
                    xmppManager.getConnection().disconnect();
                }
                xmppManager.runTask();
            }

        };
        addTask(runnable);
    }

    public XMPPTCPConnection getConnection() {
        return connection;
    }

    public void setConnection(XMPPTCPConnection connection) {
        this.connection = connection;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ConnectionListener getConnectionListener() {
        return connectionListener;
    }

    public StanzaListener getNotificationPacketListener() {
        return notificationPacketListener;
    }

    public void startReconnectionThread() {
        synchronized (reconnection) {
            if (!reconnection.isAlive()) {
                reconnection.setName("Xmpp Reconnection Thread");
                reconnection.start();
            }
        }
    }

    public Handler getHandler() {
        return handler;
    }

    public void reregisterAccount() {
        removeAccount();
        submitLoginTask();
        runTask();
    }

    public List<Runnable> getTaskList() {
        return taskList;
    }

    public Future<?> getFutureTask() {
        return futureTask;
    }

    public void runTask() {
        Log.d(LOGTAG, "runTask()...");
        synchronized (taskList) {
            running = false;
            futureTask = null;
            if (!taskList.isEmpty()) {
                Runnable runnable = (Runnable) taskList.get(0);
                taskList.remove(0);
                running = true;
                futureTask = taskSubmitter.submit(runnable);
                if (futureTask == null) {
                    taskTracker.decrease();
                }
            }
        }
        taskTracker.decrease();
        Log.d(LOGTAG, "runTask()...done");
    }

    private String newRandomUUID() {
        String uuidRaw = UUID.randomUUID().toString();
        return uuidRaw.replaceAll("-", "");
    }

    private boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    private boolean isAuthenticated() {
        return connection != null && connection.isConnected()
                && connection.isAuthenticated();
    }

    private boolean isRegistered() {
        return sharedPrefs.contains(Constants.XMPP_USERNAME)
                && sharedPrefs.contains(Constants.XMPP_PASSWORD);
    }

    private void submitConnectTask() {
        Log.d(LOGTAG, "submitConnectTask()...");
        addTask(new ConnectTask());
    }

    private void submitRegisterTask() {
        Log.d(LOGTAG, "submitRegisterTask()...");
        submitConnectTask();
        addTask(new RegisterTask());
    }

    private void submitLoginTask() {
        Log.d(LOGTAG, "submitLoginTask()...");
        submitRegisterTask();
        addTask(new LoginTask());
    }

    private void addTask(Runnable runnable) {
        Log.d(LOGTAG, "addTask(runnable)...");
        taskTracker.increase();
        synchronized (taskList) {
            if (taskList.isEmpty() && !running) {
                running = true;
                futureTask = taskSubmitter.submit(runnable);
                if (futureTask == null) {
                    taskTracker.decrease();
                }
            } else {
                taskList.add(runnable);
            }
        }
        Log.d(LOGTAG, "addTask(runnable)... done");
    }

    private void removeAccount() {
        Editor editor = sharedPrefs.edit();
        editor.remove(Constants.XMPP_USERNAME);
        editor.remove(Constants.XMPP_PASSWORD);
        editor.apply();
    }

    /**
     * A runnable task to connect the server. 
     */
    private class ConnectTask implements Runnable {

        final XmppManager xmppManager;

        private ConnectTask() {
            this.xmppManager = XmppManager.this;
        }

        public void run() {
            Log.i(LOGTAG, "ConnectTask.run()...");
            boolean connected = false;
            if (!xmppManager.isConnected()) {
                // Create the configuration for this new connection
                XMPPTCPConnectionConfiguration.Builder connConfig = XMPPTCPConnectionConfiguration.builder();

                connConfig.setHost(xmppHost);
                connConfig.setServiceName(xmppHost);
                connConfig.setPort(xmppPort);
                connConfig.setDebuggerEnabled(true);
                connConfig.setUsernameAndPassword(username, password);
                connConfig.allowEmptyOrNullUsernames();
		if (BuildConfig.DEBUG) {
			connConfig.setDebuggerEnabled(true);
		}
                connConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.required);
                //connConfig.setSASLAuthenticationEnabled(false);

                connConfig.setCompressionEnabled(false);
                connConfig.setSendPresence(true);
                //connConfig.setEnabledSSLProtocols(new String[] {"SSLv3","TLSv1.2"});
                HostnameVerifier verifier = new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        if (BuildConfig.DEBUG) {
                            Log.d(LOGTAG, "Hostname: " + hostname);
                        }
                        return true;
                    }
                };
                TrustManager[] trustAllCerts = new TrustManager[] {
                     new X509TrustManager() {
                         public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                          return new X509Certificate[0];
                         }
                         public void checkClientTrusted(
                        X509Certificate[] certs,
                        String authType) {
                         }
                         public void checkServerTrusted(
                        X509Certificate[] certs,
                        String authType) {
                         }
                     }
                };
                connConfig.setHostnameVerifier(verifier);

                SSLContext context = null;
                try {
                    context = SSLContext.getInstance("SSL");
                    connConfig.setCustomSSLContext(context);
                } catch (NoSuchAlgorithmException e) {
                    Log.e(LOGTAG, "XMPP Algorithm failed", e);
                }
                try {
                    assert context != null;
                    context.init(null, trustAllCerts, new java.security.SecureRandom());
                } catch (KeyManagementException e) {
                    Log.e(LOGTAG, "XMPP Key Management failed", e);
                }

                //SASLAuthentication.unregisterSASLMechanism("PLAIN");
		//AbstractXMPPConnection connection = new AbstractXMPPConnection(connConfig.build());
                XMPPTCPConnection connection = new XMPPTCPConnection(connConfig.build());

                xmppManager.setConnection(connection);

                try {
                    // Connect to the server
                    Log.i(LOGTAG, "XMPP trying connect");
		    /*
                    connection.addConnectionListener(connectionListener);
		    connection.setPacketReplyTimeout(10000);
		    connection.addStanzaAcknowledgedListener(new StanzaListener() {
		    @Override
	            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
			    String id = packet.getStanzaId();
			    if ((id == null) || (id == "")) {
				return;
			    }
			    //stanzaAcknowledged(id);
			}
		    });

    ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
    reconnectionManager.setEnabledPerDefault(true);
    reconnectionManager.enableAutomaticReconnection();
                    */
		    connection.connect().login();
                    Log.i(LOGTAG, "XMPP connected successfully");

                    // packet provider
                    ProviderManager.addIQProvider("notification",
                            "androidpn:iq:notification",
                            new NotificationIQProvider());
                    connected = true;
                } catch (XMPPException e) {
                    Log.e(LOGTAG, "XMPP connection failed", e);
                } catch (SmackException e) {
                    Log.e(LOGTAG, "XMPP smack failed", e);
                } catch (IOException e) {
                    Log.e(LOGTAG, "XMPP io connection failed", e); 
                } 

                if (connected) {
                    xmppManager.runTask();
                }
            } else {
                Log.i(LOGTAG, "XMPP connected already");
                xmppManager.runTask();
            }
        }
    }

    /**
     * A runnable task to register a new user onto the server. 
     */
    private class RegisterTask implements Runnable {

        final XmppManager xmppManager;

        private RegisterTask() {
            xmppManager = XmppManager.this;
        }

        public void run() {
            Log.i(LOGTAG, "RegisterTask.run()...");

            if (!xmppManager.isRegistered()) {
                final String newUsername = newRandomUUID();
                final String newPassword = newRandomUUID();


                Map<String, String> attributes = new HashMap<String, String>();
                attributes.put("username", newUsername);
                attributes.put("password", newPassword);
                //registration.setAttributes(attributes);
                //registration.setAttributes();

                //registration.addAttribute("username", newUsername);
                //registration.addAttribute("password", newPassword);
                Registration registration = new Registration(attributes);
                registration.setType(IQ.Type.set);
                registration.setTo(xmppHost);
                StanzaFilter packetFilter = new AndFilter(new StanzaIdFilter(
                        registration.getStanzaId()), new StanzaTypeFilter(
                        IQ.class));

                StanzaListener packetListener = new StanzaListener() {

                    public void processPacket(Stanza packet) {
                        Log.d("RegisterTask.PcktLstnr",
                                "processPacket().....");
                        Log.d("RegisterTask.PcktLstnr", "packet="
                                + packet.toXML());

                        if (packet instanceof IQ) {
                            IQ response = (IQ) packet;
                            if (response.getType() == IQ.Type.error) {
                                if (!response.getError().toString().contains(
                                        "409")) {
                                    Log.e(LOGTAG,
                                            "Unknown error while registering XMPP account! "
                                                    + response.getError()
                                                            .getCondition());
                                }
                            } else if (response.getType() == IQ.Type.result) {
                                xmppManager.setUsername(newUsername);
                                xmppManager.setPassword(newPassword);
                                Log.d(LOGTAG, "username=" + newUsername);
                                Log.d(LOGTAG, "password=" + newPassword);

                                Editor editor = sharedPrefs.edit();
                                editor.putString(Constants.XMPP_USERNAME,
                                        newUsername);
                                editor.putString(Constants.XMPP_PASSWORD,
                                        newPassword);
                                editor.apply();
                                Log
                                        .i(LOGTAG,
                                                "Account registered successfully");
                                xmppManager.runTask();
                            }
                        }
                    }
                };

                connection.addAsyncStanzaListener(packetListener, packetFilter);


                try {
                    connection.sendStanza(registration);
                } catch (SmackException.NotConnectedException e) {
                    Log.e(LOGTAG, "XMPP failed to send ", e);
                }

            } else {
                Log.i(LOGTAG, "Account registered already");
                xmppManager.runTask();
            }
        }
    }

    /**
     * A runnable task to log into the server. 
     */
    private class LoginTask implements Runnable {

        final XmppManager xmppManager;

        private LoginTask() {
            this.xmppManager = XmppManager.this;
        }

        public void run() {
            Log.i(LOGTAG, "LoginTask.run()...");

            if (!xmppManager.isAuthenticated()) {
                Log.d(LOGTAG, "username=" + username);
                Log.d(LOGTAG, "password=" + password);

                try {
                    xmppManager.getConnection().login(
                            xmppManager.getUsername(),
                            xmppManager.getPassword(), XMPP_RESOURCE_NAME);
                    Log.d(LOGTAG, "Loggedn in successfully");

                    // connection listener
                    if (xmppManager.getConnectionListener() != null) {
                        xmppManager.getConnection().addConnectionListener(
                                xmppManager.getConnectionListener());
                    }

                    // packet filter
                    StanzaFilter packetFilter = new StanzaTypeFilter(
                            NotificationIQ.class);
                    // packet listener
                    StanzaListener packetListener = xmppManager
                            .getNotificationPacketListener();
                    connection.addAsyncStanzaListener(packetListener, packetFilter);

                    xmppManager.runTask();

                } catch (XMPPException e) {
                    Log.e(LOGTAG, "LoginTask.run()... xmpp error");
                    Log.e(LOGTAG, "Failed to login to xmpp server. Caused by: "
                            + e.getMessage());
                    String INVALID_CREDENTIALS_ERROR_CODE = "401";
                    String errorMessage = e.getMessage();
                    if (errorMessage != null
                            && errorMessage
                                    .contains(INVALID_CREDENTIALS_ERROR_CODE)) {
                        xmppManager.reregisterAccount();
                        return;
                    }
                    xmppManager.startReconnectionThread();

                } catch (Exception e) {
                    Log.e(LOGTAG, "LoginTask.run()... other error");
                    Log.e(LOGTAG, "Failed to login to xmpp server. Caused by: "
                            + e.getMessage());
                    xmppManager.startReconnectionThread();
                }

            } else {
                Log.i(LOGTAG, "Logged in already");
                xmppManager.runTask();
            }

        }
    }

}
