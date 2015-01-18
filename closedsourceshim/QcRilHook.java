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

import net.scintill.qcrilhook.IQcRilHook;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class QcRilHook implements IQcRilHook {

    /*package*/ Object mQcRilHook;
    /*package*/ Context mCtx;

    public QcRilHook(Context ctx) {
        try {
            mQcRilHook = Class.forName("com.qualcomm.qcrilhook.QcRilHook")
                .getConstructor(Context.class).newInstance(ctx);
            mCtx = ctx;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setFieldTestMode(int sub, byte ratType, int enable) throws IOException {
        try {
            boolean b = (Boolean)mQcRilHook.getClass().getMethod("qcRilSetFieldTestMode", int.class, byte.class, int.class).invoke(mQcRilHook, sub, ratType, enable);
            if (!b) {
                throw new IOException();
            }
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            // no exceptions expected
            throw new RuntimeException(e);
        }
    }

    @Override
    public String[] getAvailableConfigs(String device) throws IOException {
        try {
            String[] configs = (String[])mQcRilHook.getClass().getMethod("qcRilGetAvailableConfigs", String.class).invoke(mQcRilHook, device);
            if (configs == null) {
                throw new IOException();
            }
            return configs;
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            // no exceptions expected
            throw new RuntimeException(e);
        }
    }

}
