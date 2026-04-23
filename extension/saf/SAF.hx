package extension.saf;

#if android
import lime.system.JNI;
#end

class SAF {
    #if android
    private static var _open_jni:Dynamic = null;
    private static var _list_jni:Dynamic = null;
    private static var _currentCallback:SAFCallback = null;
    #end

    public static function open(onResult:String->Void, onError:String->Void):Void {
        #if android
        try {
            if (_open_jni == null) {
                _open_jni = JNI.createStaticMethod("extension/saf/SAFHelper", "openSAF", "(Lorg/haxe/lime/HaxeObject;)V");
            }
            _currentCallback = new SAFCallback(onResult, onError);
            if (_open_jni != null) _open_jni(_currentCallback);
        } catch(e:Dynamic) {
            if (onError != null) onError(Std.string(e));
        }
        #end
    }

    public static function listFiles(uriString:String):Array<String> {
        #if android
        try {
            if (_list_jni == null) {
                _list_jni = JNI.createStaticMethod("extension/saf/SAFHelper", "listFiles", "(Ljava/lang/String;)[Ljava/lang/String;");
            }
            if (_list_jni == null) return [];
            
            var nativeArray:Dynamic = _list_jni(uriString);
            var hxArray:Array<String> = [];
            if (nativeArray != null) {
                for (i in 0...Std.int(nativeArray.length)) {
                    hxArray.push(nativeArray[i]);
                }
            }
            return hxArray;
        } catch(e:Dynamic) {
            return [];
        }
        #end
        return [];
    }
}

@:keep
class SAFCallback {
    var cb_ok:String->Void;
    var cb_err:String->Void;

    public function new(ok:String->Void, err:String->Void) {
        this.cb_ok = ok;
        this.cb_err = err;
    }

    public function onResult(uri:String):Void {
        haxe.MainLoop.runInMainThread(function() {
            if (cb_ok != null) cb_ok(uri);
        });
    }

    public function onError(msg:String):Void {
        haxe.MainLoop.runInMainThread(function() {
            if (cb_err != null) cb_err(msg);
        });
    }
}
