/**
 * Copyright (c) 2015 Joey Hewitt <joey@joeyhewitt.com>
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

package net.scintill.qcrilhook.closedsourceshim;

import android.content.Context;

import net.scintill.qcrilhook.ISemcRilHook;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class SemcRilHook extends QcRilHook implements ISemcRilHook {

    protected Object mSemcRilExtConfigInfo;
    protected Object mSemcRilExtFtm;
    protected Object mSemcRilExtNvItem;

    public SemcRilHook(Context ctx) {
        super(ctx);

        try {
            mSemcRilExtConfigInfo = Class.forName("com.sonyericsson.android.semcrilextension.SemcRilExtConfigInfo")
                    .getConstructor(Context.class, Class.forName("com.qualcomm.qcrilhook.IQcSemcServiceConnected"))
                    .newInstance(ctx, null);
            mSemcRilExtFtm = Class.forName("com.sonyericsson.android.semcrilextension.SemcRilExtFTM")
                    .getConstructor(Context.class, Class.forName("com.qualcomm.qcrilhook.IQcSemcServiceConnected"))
                    .newInstance(ctx, null);
            mSemcRilExtNvItem = Class.forName("com.sonyericsson.android.semcrilextension.SemcRilExtNvItem")
                    .getConstructor(Context.class, Class.forName("com.qualcomm.qcrilhook.IQcSemcServiceConnected"))
                    .newInstance(ctx, null);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getCipherIndicator() throws IOException {
        return getConfigInfoByteArray("getCipherIndicator");
    }

    @Override
    public byte[] getActiveBand() throws IOException {
        return getConfigInfoByteArray("getActiveBand");
    }

    @Override
    public byte[] getSpeechCodec() throws IOException {
        return getConfigInfoByteArray("getSpeechCodec");
    }

    @Override
    public byte[] registerForFtmLiveUpdate(FtmNetworkType type) throws IOException {
        try {
            return (byte[])mSemcRilExtFtm.getClass().getMethod("RegisterforFTMLiveUpdate", String.class)
                    .invoke(mSemcRilExtFtm, type.toString());
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof IOException) {
                throw (IOException)e.getTargetException();
            }
            // no other expected exceptions
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] unregisterForFtmLiveUpdate(FtmNetworkType type) throws IOException {
        try {
            return (byte[])mSemcRilExtFtm.getClass().getMethod("UnRegisterforFTMLiveUpdate", String.class)
                    .invoke(mSemcRilExtFtm, type.toString());
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof IOException) {
                throw (IOException)e.getTargetException();
            }
            // no other expected exceptions
            throw new RuntimeException(e);
        }
    }

    @Override
    public long readNvSemc(int itemId) throws IOException {
        try {
            return (Long)mSemcRilExtNvItem.getClass().getMethod("getNvItem", int.class).invoke(mSemcRilExtNvItem, itemId);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof IOException) {
                throw (IOException) e.getTargetException();
            }
            // no other expected exceptions
            throw new RuntimeException(e);
        }
    }

    protected byte[] getConfigInfoByteArray(String getterName) throws IOException {
        try {
            return (byte[])mSemcRilExtConfigInfo.getClass().getMethod(getterName).invoke(mSemcRilExtConfigInfo);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof IOException) {
                throw (IOException)e.getTargetException();
            }
            // no other expected exceptions
            throw new RuntimeException(e);
        }
    }
}
