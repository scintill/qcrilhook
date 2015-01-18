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

import net.scintill.qcrilhook.IQcNvItems;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class QcNvItems implements IQcNvItems {

    protected Object mQcNvItems;

    public QcNvItems(QcRilHook rilHook) {
        try {
            mQcNvItems = Class.forName("com.qualcomm.qcnvitems.QcNvItems")
                    .getConstructor(Context.class).newInstance(rilHook.mCtx);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AutoAnswer getAutoAnswer() throws IOException {
        try {
            Object o = mQcNvItems.getClass().getMethod("getAutoAnswerStatus").invoke(mQcNvItems);
            AutoAnswer aa = new AutoAnswer();
            aa.enable = (Boolean)o.getClass().getMethod("isEnabled").invoke(o);
            aa.rings = (Short)o.getClass().getMethod("getRings").invoke(o);
            return aa;
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof IOException) {
                throw (IOException)e.getTargetException();
            }

            // no other exceptions expected
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] readNv(int itemId) throws IOException {
        try {
            Method m = mQcNvItems.getClass().getDeclaredMethod("doNvRead", int.class);
            m.setAccessible(true);
            return (byte[])m.invoke(mQcNvItems, itemId);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof IOException) {
                throw (IOException)e.getTargetException();
            }

            // no other exceptions expected
            throw new RuntimeException(e);
        }
    }
}
