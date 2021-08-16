package simon.tuke;
import android.os.AsyncTask;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import javax.security.auth.callback.Callback;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Context;
import android.util.Log;
import android.view.ViewStub;

public class Tuke
{

	private static DiskCache disk;
	
	private static OnException error=new OnException() {
		@Override
		public void onIOError(IOException e) {
			Log.e("tuke1",e.toString());
		}

		@Override
		public void onClassNFError(ClassNotFoundException e) {
			Log.e("tuke2",e.toString());
		}
	};
	
	public static void init(Context in)
	{
		init("TUKE", in.getFilesDir().getAbsolutePath().toString());
	}

    public static void init(String name, String path)
	{
		disk = new DiskCache(name, path);
	}
	public static void setException(OnException in)
	{
		error = in;
	}
	

	private static String keytonew(String key)
	{
		return key.replaceAll(File.separator, "|");
	}
	

	public static <T extends Serializable> void write(String key, T  in)
	{
		key = keytonew(key);
		try
		{
			disk.write(key, in);
		}
		catch (IOException e)
		{
			if (error != null)
				error.onIOError(e);
		}
	}
	
	public static void write( String key, Bitmap bit)
	{
		key = keytonew(key);
		try
		{
			disk.saveBitmap(key, bit);
		}
		catch (IOException e)
		{
			if (error != null)
				error.onIOError(e);
		}

	}
	
	public static Bitmap getBitmap(String key, Bitmap def)
	{
		key = keytonew(key);
		Bitmap a=disk.getBitmap(key);
			if (a== null)
				return def;
				return a;
	}
	public static <T extends Serializable> T get(String key)  {
   return get(key,null);
     }
	public static <T extends Serializable> T get(String key,T def)  {
		try{
			String mkey=keytonew(key);
				T b=disk.get(mkey);
				if(b!=null)
					return b;
		}catch(IOException e){
			if(error!=null)
				error.onIOError(e);
		}catch(ClassNotFoundException e){
            if(error!=null)
				error.onClassNFError(e);
		}
		return def;
	}
	
	public static void clearDisk(String key)
	{
		key = keytonew(key);
		disk.delete(key);
	}
	public static void clearDisk()
	{
		disk.delete();
	}
	public interface Callback
	{
        void apply();
    }
	public interface OnException
	{
		void onIOError(IOException e);
        void onClassNFError(ClassNotFoundException e);
	}
	
	public static  void putBitmapAsync( final String key, final Bitmap value, final Callback callback)
	{
        new AsyncTask<Void, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params)
			{
                Tuke.write(key, value);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean success)
			{
                super.onPreExecute();
                if (callback != null)
				 callback.apply();
                
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }
}
