package simon.tuke;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import android.graphics.Bitmap;
import java.io.FileNotFoundException;
import android.graphics.BitmapFactory;
import android.content.Context;

public class DiskCache {
	File mfile;
	public DiskCache(String name,String mpath) {
		path=mpath;basename=name;
		 mfile=new File(mpath+File.separator+name);
		if(!mfile.exists())
		mfile.mkdirs();
	}
	
	private String basename;
	private String path;
	private  File file(String a) throws IOException{
		File doo=new File(a);
		if(doo.exists())
		doo.delete();
		doo.createNewFile();
		return doo;
	}
	private  String string(String k){
		return path+File.separator+basename+File.separator+k+".dat";
	}
	public Bitmap getBitmap(String key){
		return BitmapFactory.decodeFile(string(key));
	}
	
    public  void saveBitmap(String key,Bitmap mBitmap) throws FileNotFoundException, IOException {
        File filePic=new File(string(key));
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
    }
	
	public <T extends Serializable>  void write(String key,T obj) throws IOException {
		//1,明确存储对象的文件。
		FileOutputStream fos = new FileOutputStream(file(string(key)));
		//2，给操作文件对象加入写入对象功能。
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		//3，调用了写入对象的方法。
		oos.writeObject(obj);
		//关闭资源。
		oos.close();
	}
	public <T> T get(String key) throws IOException, ClassNotFoundException {
		//1,定义流对象关联存储了对象文件。
		FileInputStream fis = new FileInputStream(new File(string(key)));
		//2,建立用于读取对象的功能对象。
		ObjectInputStream ois = new ObjectInputStream(fis);
	     return (T)  ois.readObject();
	}
	public  void delete(String key) {
		File f= new File(string(key));
		if(f.exists())
		f.delete();
	}
	public  void delete() {
		if(mfile.exists())
	  deleteFiles(mfile);
	}
	private  void deleteFiles(File file){
        for (File f: file.listFiles()){
            if (f.isDirectory())
                deleteFiles(f);
            else 
                f.delete();
        }
        file.delete();
    }
	
}
