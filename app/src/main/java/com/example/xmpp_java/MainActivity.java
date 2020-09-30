package com.example.xmpp_java;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.example.xmpp_java.utils.FileUtils;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import static android.os.SystemClock.sleep;
import static org.jivesoftware.smackx.pubsub.AccessModel.presence;

public class MainActivity extends AppCompatActivity {

    public static final String DOMAIN="ip-172-31-41-41.ap-south-1.compute.internal";
    public static final String HOST="team-grevity.in";
    XmppConnectionListener connectionListener = new XmppConnectionListener();
    AbstractXMPPConnection connection;
    public static FileTransferManager manager;
    ChatManager chatManager;
    Roster roster;
    String username = "test";
    String password = "test";
    private static final int REQUEST_PLACE_PICKER = 1;
    private static final int REQUEST_IMAGE_PICKER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button connectBtn = (Button) findViewById(R.id.connectBtn);
        final Button sendMessageBtn = (Button) findViewById(R.id.sendMessageBtn);
        final Button pickImageBtn = (Button) findViewById(R.id.pickImageBtn);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    init();
                    connect();
                } catch (XmppStringprepException e) {
                    e.printStackTrace();
                }
            }
        });

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                EntityBareJid jid = null;
                try {
                    jid = JidCreate.entityBareFrom("gurkaran@"+DOMAIN);
                } catch (XmppStringprepException e) {
                    e.printStackTrace();
                }
                Chat chat = chatManager.chatWith(jid);
                chatManager.addIncomingListener(new IncomingChatMessageListener() {
                    @Override
                    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                        Log.d("Incoming", message.getBody());
                    }
                });
                try {
                    chat.send("Hello!");
                    Log.d("XMPP Address", chat.getXmppAddressOfChatPartner().toString());
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        pickImageBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View v){
                String rootPath = getExternalFilesDir(null)
                        .getAbsolutePath();
                File root = new File(rootPath);
                if (!root.exists()) {
                    Log.d("Root Exists", "not exists");
                    if(root.mkdirs()){
                        Log.d("Dir Creation", "created");
                    }else{
                        Log.d("Dir Creation", "not created");
                    }
                }else {
                    Log.d("Root Exists", "exists");
                }
                File f = new File(rootPath + "-test-xmpp.txt");
                if (f.exists()) {
                    f.delete();
                }
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(f);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    sendFile(f);
                } catch (XmppStringprepException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void init() throws XmppStringprepException {
        Log.i("XMPP", "Initializing!");
        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setUsernameAndPassword(username, password);
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        configBuilder.setHost(HOST);
        configBuilder.setXmppDomain(DOMAIN);
        configBuilder.setPort(5222);
        connection = new XMPPTCPConnection(configBuilder.build());
        connection.addConnectionListener(connectionListener);
    }
    public void connect() {
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... arg0) {
                try {
                    connection.connect();
                    login();
                    chatManager = ChatManager.getInstanceFor(connection);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (XMPPException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        connectionThread.execute();
    }
    public void login() {
        try {
            if (connection.isAuthenticated()) {
                Log.e("TAG", "User already logged in");
                return;
            }
            connection.login(username, password);
            manager = FileTransferManager.getInstanceFor(connection);
            roster = Roster.getInstanceFor(connection);
            roster.addRosterListener(new RosterListener() {
                @Override
                public void entriesAdded(Collection<Jid> addresses) {
                    Log.d("entriesAdded", addresses.toString());
                    MainActivity.this.sendBroadcast(new Intent("ENTRIES_ADDED"));
                }

                @Override
                public void entriesUpdated(Collection<Jid> addresses) {
                    Log.d("entriesUpdated", addresses.toString());
                }

                @Override
                public void entriesDeleted(Collection<Jid> addresses) {
                    Log.d("entriesDeleted", addresses.toString());
                }

                @Override
                public void presenceChanged(Presence presence) {
                    Log.d("presenceChanged", presence.toString());
                    //Resource from presence
                    String resource = presence.getFrom().getResourceOrEmpty().toString();
                    //Update resource part for user in DB or preferences
                    //...
                    Log.d("presenceChanged", resource);
                }
            });
        } catch (XMPPException | SmackException | IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFile(File file) throws XmppStringprepException, InterruptedException {
//        Log.d("Service", FileTransferNegotiator.isServiceEnabled(connection))
//        OutgoingFileTransfer transfer = null;
//        try {
//            transfer = manager.createOutgoingFileTransfer(JidCreate.entityFullFrom("gurkaran@"+DOMAIN+"/resource"));
//        } catch (XmppStringprepException e) {
//            e.printStackTrace();
//        }
//        try {
//            assert transfer != null;
//            transfer.sendFile(file, "You won't believe this!");
//        } catch (SmackException e) {
//            e.printStackTrace();
//        }
//        while(!transfer.isDone()) {
//            if (transfer.getStatus().equals(FileTransfer.Status.error)) {
//                System.out.println("ERROR!!! " + transfer.getError());
//            } else {
//                System.out.println(transfer.getStatus());
//                System.out.println(transfer.getProgress());
//            }
//            sleep(1000);
//        }
        FileTransferManager fileTransferManager = FileTransferManager.getInstanceFor(connection);
        OutgoingFileTransfer transfer = fileTransferManager.createOutgoingFileTransfer(JidCreate.entityFullFrom("gurkaran@"+ DOMAIN+"/Gurkarans-MacBook-Pro"));
        try {
            transfer.sendFile(file,"fielllll");
        } catch (SmackException e) {
            e.printStackTrace();
        }
        while(!transfer.isDone()) {
            Log.d("xmpp status", String.valueOf(transfer.getProgress()));
            Thread.sleep(1000);
        }
        if(transfer.getStatus().equals(FileTransfer.Status.refused) || transfer.getStatus().equals(FileTransfer.Status.error)
                || transfer.getStatus().equals(FileTransfer.Status.cancelled)){
            Log.d("File Transfer", "Error Status" + transfer.getStatus());
            Log.d("File Transfer", "Error  " + transfer.getError());
        } else {
            Log.d("File Transfer", "Success: " + transfer.getStatus());
            Log.d("File Transfer", "Progress: "+transfer.getProgress());
        }
    }

//    @RequiresApi(api = Build.VERSION_CODES.P)
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        if (resultCode == Activity.RESULT_OK) {
//            if (requestCode == REQUEST_IMAGE_PICKER) {
//                try {
//
////                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),data.getData());
////                    ImageView my_img_view = (ImageView) findViewById (R.id.imageView);
////                    my_img_view.setImageBitmap(bitmap);
////                    File fileToSend = createSentImageFile(this, fileName,bitmap);
////                    Log.d("File Path", fileToSend.getAbsolutePath().toString());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
////                assert data != null;
////                Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromStream(MainActivity.this, data.getData(), 100, 100);
////                try {
////                    assert bitmap != null;
////                    File fileToSend = createSentImageFile(MainActivity.this, fileName, bitmap);
////                    final TextView pathTextView = (TextView) findViewById(R.id.filePath);
////                    pathTextView.setText(fileToSend.getAbsolutePath());
//////                    sendFile(fileToSend);
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data);
////        if (requestCode == 10) {
////            if (resultCode == RESULT_OK) {
////                assert data != null;
////                String path = Objects.requireNonNull(data.getData()).getPath();
////                final TextView pathTextView = (TextView) findViewById(R.id.filePath);
////                pathTextView.setText(path);
////                try {
////                    Bitmap bitmap = MediaStore.Images.Media
////                            .getBitmap(MainActivity.this.getContentResolver(), data.getData());
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////            }
////        }
//    }

    private File createSentImageFile(Context context, String fileName, Bitmap bitmap) throws IOException {
        File file = new File(FileUtils.getSentImagesDir(context), fileName + FileUtils.IMAGE_EXTENSION);
        FileOutputStream outputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        outputStream.flush();
        outputStream.close();

        return file;
    }
}

class XmppConnectionListener implements ConnectionListener {
    @Override
    public void connected(XMPPConnection connection) {
        Log.d("xmpp", "Connected!");
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        Log.d("xmpp", "Authenticated!");
    }

    @Override
    public void connectionClosed() {
        Log.d("xmpp", "ConnectionClosed!");
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        Log.d("xmpp", "ConnectionClosedOn Error!");
    }
}
