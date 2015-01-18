# qcrilhook

Tinkering with the qcrilhook functions available on modern Qualcomm-based Android phones.  These are implemented through Java libraries in /system/framework, that communicate with a Intent endpoint living in the com.android.phone process, which sends a RIL_REQUEST_OEM_HOOK_RAW to the native RIL code.

So far, I've implemented a wrapper around a few methods in the closed-source and undocumented Java libraries.  Unfortunately there is not much exciting that really works (yet?)  I'm not sure if I'm doing something wrong, need to enable a debug flag somehow, need engineering-mode binaries, etc.

TODO Do some more testing with qcril_log_adb_on = 1 in the RIL .so (it's a 1-byte debug flag exported in libril-qc-qmi-1.so's symbol table, which causes extra output to the logcat log)

## Dependencies

You'll need some variant of the following, depending on your exact usage:

```xml
<uses-library android:name="com.qualcomm.qcrilhook" android:required="true" />
<uses-library android:name="com.qualcomm.qcnvitems" android:required="true" />
<uses-library android:name="semcrilextension" android:required="true" />
```
    
These refer to libraries that are installed in /system/framework, so the compiled app requires a ROM that includes them (such as many shipped ROMs for Qualcomm devices -- AOSP etc. probably won't have these libraries.)

## Generic Qualcomm Features

* Query "auto answer" configuration from NV memory ("generic failure" from RIL on my device)
* Query arbitrary NV items ("generic failure")
* Enable a "field test" mode. Works, but the format of the response packets is currently unknown. They can be seen in the radio log (adb logcat -b radio). I believe there's an Intent you can listen for to receive the packets into the app, but I haven't implemented it since I can't do much with the responses.
* Get available "configurations" (?) ("not supported" or something similar on my device)

## SEMC (Sony) Features

* Cipher indicator ("generic failure")
* Get active bands - generic byte array, unknown meaning, but could probably be reversed from the service menu app (dial `*#*#7378423#*#*` on Sony Xperia Z1 Compact)
* Get speech codecs - same as above
* Sony-specific field test mode - similar situation as the generic Qualcomm field test.  I see an intent "semc.intent.action.ACTION_UNSOL_SEMC_FIELD_TEST_MODE" with extra "payload" (byte[]), but have not implemented reception.
* Sony-specific NV reading - doesn't work on my device

## License

The MIT License (MIT)

Copyright (c) 2015 Joey Hewitt <joey@joeyhewitt.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
