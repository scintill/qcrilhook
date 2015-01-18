/**
 * Copyright (c) 2014 Joey Hewitt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.scintill.simfilereader;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.utils.Helpers;

import net.scintill.qcrilhook.IQcNvItems;
import net.scintill.qcrilhook.ISemcRilHook;
import net.scintill.qcrilhook.closedsourceshim.QcNvItems;
import net.scintill.qcrilhook.closedsourceshim.QcRilHook;
import net.scintill.simio.CardApplication;
import net.scintill.simio.CommandsInterfaceFactory;
import net.scintill.simio.telephony.CommandsInterface;
import net.scintill.simio.telephony.uicc.IccUtils;
import net.scintill.simio.telephony.uicc.SIMRecords;
import net.scintill.simio.telephony.uicc.UiccCardApplication;

import java.io.IOException;

public class MyActivity extends Activity {

    private final static String TAG = "SIMFileReader";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        // do files test in another thread, so the UI doesn't get blocked
        HandlerThread handlerThread = new HandlerThread("SIMFilesTest");
        handlerThread.start();
        // getLooper() can block, but it shouldn't be as bad as the blocking on SuperSU before.
        // Maybe I'm doing something wrong here, though.
        new Handler(handlerThread.getLooper()).post(new Runnable() {
            @Override
            public void run() {
                filesTests();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private CommandsInterface mCommandsInterface;

    private void filesTests() {
        try {
            if (Helpers.checkSu()) {
                // XXX get SIM application ID?
                mCommandsInterface = CommandsInterfaceFactory.factory(getApplicationContext());
                UiccCardApplication cardApplication = new CardApplication(mCommandsInterface);
                final SIMRecords records = new SIMRecords(cardApplication, this.getApplicationContext(), mCommandsInterface);

                records.registerForRecordsLoaded(new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                String results =
                                    "MSISDN=" + records.getMsisdnNumber()+"\n"+
                                    "TMSI=" + IccUtils.bytesToHexString(records.getTemporaryMobileSubscriberIdentity())+"\n"+
                                    "LAI=" + IccUtils.bytesToHexString(records.getLocationAreaInformation())+"\n"+
                                    "\n"+
                                    "Commands interface: "+mCommandsInterface.getClass().getSimpleName()+"\n"+
                                    mCommandsInterface.getInterfaceDebugInfo();

                                TextView textViewResults = (TextView)MyActivity.this.findViewById(R.id.results);
                                textViewResults.setText(results);

                                qcrilhookTests();
                            }
                        });
                    }
                }, 0, null);
            } else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        // technically we can work without su, if the phone process has already been injected
                        TextView textViewResults = (TextView) MyActivity.this.findViewById(R.id.results);
                        textViewResults.setText("Superuser/su not available!");
                    }
                });
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error loading SIM files", t);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCommandsInterface != null) {
            mCommandsInterface.dispose();
        }
    }

    private static ISemcRilHook rilHook = null;
    private static IQcNvItems nvItems = null;

    private void qcrilhookTests() {
        if (rilHook == null) {
            // make these persistent, because first call fails while QC service isn't connected

            rilHook = new net.scintill.qcrilhook.closedsourceshim.SemcRilHook(this.getApplicationContext()/*new OemRilCommandsInterface() {
            @Override
            public Object invokeOemRilRequestRaw(ByteBuffer b, int subscription) {
                RilExtenderCommandsInterface commandsInterface = new RilExtenderCommandsInterface(MyActivity.this.getApplicationContext());
                try {
                    commandsInterface.getIRilExtender().oemRilRequestRaw(b.array());
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }*/);
            nvItems = new QcNvItems((QcRilHook)rilHook);
        }

        /*try {
            rilHook.setFieldTestMode(0, (byte) 0, 1);
        } catch (IOException e) {
            Log.e(TAG, "IOException while setting field test mode", e);
        }

        try {
            Log.e(TAG, "Cipher indicator=" + IccUtils.bytesToHexString(rilHook.getCipherIndicator()));
        } catch (IOException e) {
            Log.e(TAG, "IOException while getting cipher indicator", e);
        }

        try {
            Log.e(TAG, "Speech codec=" + IccUtils.bytesToHexString(rilHook.getSpeechCodec()));
        } catch (IOException e) {
            Log.e(TAG, "IOException while getting speech codec", e);
        }

        try {
            Log.e(TAG, "Active band=" + IccUtils.bytesToHexString(rilHook.getActiveBand()));
        } catch (IOException e) {
            Log.e(TAG, "IOException while getting active band", e);
        }*/

        StringBuffer configs = new StringBuffer();
        try {
            for (String s : rilHook.getAvailableConfigs("GSM")) {
                configs.append(s).append("\n");
            }
            Log.e(TAG, "Avail configs=" + configs);
        } catch (IOException e) {
            Log.e(TAG, "IOException while getting avail configs", e);
        }

        /*try {
            rilHook.unregisterForFtmLiveUpdate(ISemcRilHook.NetworkType.GSM);
        } catch (IOException e) {
            Log.e(TAG, "IOException while registering FTM", e);
        }

        try {
            IQcNvItems.AutoAnswer aa = nvItems.getAutoAnswer();
            Log.d(TAG, "Auto answer " + aa.enable + " " + aa.rings);
        } catch (IOException e) {
            Log.e(TAG, "IOException while checking autoanswer", e);
        }

        try {
            Log.d(TAG, "IMEI=" + rilHook.readNvSemc(457));
        } catch (IOException e) {
            Log.e(TAG, "IOException while reading IMEI", e);
        }*/
    }

}
