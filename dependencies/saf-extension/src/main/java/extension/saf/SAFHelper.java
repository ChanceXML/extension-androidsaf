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

    public static void openSAF(HaxeObject haxeCallback) {
        callback = haxeCallback;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        Extension.mainActivity.startActivityForResult(intent, REQ_CODE);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    Extension.mainContext.getContentResolver().takePersistableUriPermission(uri, flags);
                    if (callback != null) callback.call1("onResult", uri.toString());
                }
            } else {
                if (callback != null) callback.call1("onError", "User cancelled");
            }
            return true;
        }
        return super.onActivityResult(requestCode, resultCode, data);
    }

    public static String[] listFiles(String uriString) {
        Uri uri = Uri.parse(uriString);
        DocumentFile dir = DocumentFile.fromTreeUri(Extension.mainContext, uri);
        if (dir != null && dir.isDirectory()) {
            DocumentFile[] files = dir.listFiles();
            String[] result = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                result[i] = files[i].getName() + "|" + files[i].getUri().toString();
            }
            return result;
        }
        return new String[0];
    }
}
