package extension.saf;

import android.content.Intent;
import android.app.Activity;
import android.net.Uri;
import androidx.documentfile.provider.DocumentFile;
import org.haxe.extension.Extension;
import org.haxe.lime.HaxeObject;

public class SAFHelper extends Extension {
    private static HaxeObject callback;
    private static final int REQ_CODE = 4001;

    public static void openSAF(final HaxeObject haxeCallback) {
        callback = haxeCallback;
        if (Extension.mainActivity == null) return;

        Extension.mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Extension.mainActivity == null) return;
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    Extension.mainActivity.startActivityForResult(intent, REQ_CODE);
                } catch (Exception e) {
                    if (callback != null) {
                        callback.call("onError", new Object[]{e.getMessage()});
                        callback = null;
                    }
                }
            }
        });
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri != null && Extension.mainActivity != null) {
                    try {
                        int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                        Extension.mainActivity.getContentResolver().takePersistableUriPermission(uri, flags);
                        if (callback != null) callback.call("onResult", new Object[]{uri.toString()});
                    } catch (Exception e) {
                        if (callback != null) callback.call("onError", new Object[]{e.getMessage()});
                    }
                }
            } else {
                if (callback != null) callback.call("onError", new Object[]{"User cancelled"});
            }
            callback = null;
            return true;
        }
        return super.onActivityResult(requestCode, resultCode, data);
    }
    
    public static String[] listFiles(String uriString) {
        try {
            if (Extension.mainContext == null) return new String[0];
            
            Uri uri = Uri.parse(uriString);
            DocumentFile dir = DocumentFile.fromTreeUri(Extension.mainContext, uri);
            
            if (dir != null && dir.isDirectory()) {
                DocumentFile[] files = dir.listFiles();
                if (files == null) return new String[0];
                
                String[] result = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    String type = files[i].isDirectory() ? "true" : "false";
                    result[i] = files[i].getName() + "|" + files[i].getUri().toString() + "|" + type;
                }
                return result;
            }
        } catch (Exception e) {
            return new String[0];
        }
        return new String[0];
    }
}
