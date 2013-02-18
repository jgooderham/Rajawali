package rajawali.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.zip.GZIPOutputStream;

import rajawali.AGeometry3D;
import rajawali.BaseObject3D;
import rajawali.BufferInfo;
import rajawali.SerializedObject3D;
import rajawali.animation.mesh.VertexAnimationFrame;
import rajawali.animation.mesh.VertexAnimationObject3D;
import rajawali.materials.AMaterial;
import android.os.Environment;

public class MeshExporter {
	private BaseObject3D mObject;
	private String mFileName;
	private boolean mCompressed;
	
	public enum ExportType {
		SERIALIZED,
		OBJ
	}
	
	public MeshExporter(BaseObject3D objectToExport) {
		mObject = objectToExport;
	}
	
	public void export(String fileName, ExportType type) {
		export(fileName, type, false);
	}
	
	public void export(String fileName, ExportType type, boolean compressed) {
		mFileName = fileName;
		mCompressed = compressed;
		switch(type) {
		case SERIALIZED:
			exportToSerialized();
			break;
		case OBJ:
			exportToObj();
			break;
		}
	}
	
	private void exportToObj() {
		RajLog.d("Exporting " +mObject.getName()+ " as .obj file");
		AGeometry3D g = mObject.getGeometry();
		StringBuffer sb = new StringBuffer();
		
		sb.append("# Exported by Rajawali 3D Engine for Android\n");
		sb.append("o ");
		sb.append(mObject.getName());
		sb.append("\n");
		
		FloatBuffer buff = (FloatBuffer)(g.getBuffer(AMaterial.ATTR_POSITION).buffer);
		for(int i=0; i<buff.capacity(); i+=3) {
			sb.append("v ");
			sb.append(buff.get(i));
			sb.append(" ");
			sb.append(buff.get(i+1));
			sb.append(" ");
			sb.append(buff.get(i+2));
			sb.append("\n");
		}
		
		sb.append("\n");

		buff = (FloatBuffer)(g.getBuffer(AMaterial.ATTR_TEXTURECOORD).buffer);
		for(int i=0; i<buff.capacity(); i+=2) {
			sb.append("vt ");
			sb.append(buff.get(i));
			sb.append(" ");
			sb.append(buff.get(i+1));
			sb.append("\n");
		}
		
		sb.append("\n");

		buff = (FloatBuffer)(g.getBuffer(AMaterial.ATTR_NORMAL).buffer);
		for(int i=0; i<buff.capacity(); i+=3) {
			sb.append("vn ");
			sb.append(buff.get(i));
			sb.append(" ");
			sb.append(buff.get(i+1));
			sb.append(" ");
			sb.append(buff.get(i+2));
			sb.append("\n");
		}
		
		sb.append("\n");
		
// TODO: jp - fixme!
//		boolean isIntBuffer = g.getIndices() instanceof IntBuffer;
//		
//		for(int i=0; i<g.getIndices().capacity(); i++) {
//			if(i%3 == 0)
//				sb.append("\nf ");
//			int index = isIntBuffer ? ((IntBuffer)g.getIndices()).get(i) + 1 : ((ShortBuffer)g.getIndices()).get(i) + 1;
//			sb.append(index);
//			sb.append("/");
//			sb.append(index);
//			sb.append("/");
//			sb.append(index);
//			sb.append(" ");
//		}
		
		try
	    {
			File sdcardStorage = Environment.getExternalStorageDirectory();
			String sdcardPath = sdcardStorage.getParent()
					+ java.io.File.separator + sdcardStorage.getName();

			File f = new File(sdcardPath + File.separator + mFileName);
	        FileWriter writer = new FileWriter(f);
	        writer.append(sb.toString());
	        writer.flush();
	        writer.close();
	        
	        RajLog.d(".obj export successful: " + sdcardPath + File.separator + mFileName);
	    }
	    catch(IOException e)
	    {
	         e.printStackTrace();
	    }
	}
	
	/**
	 * Make sure this line is in your AndroidManifer.xml file, under <manifest>:
	 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
	 */
	private void exportToSerialized() {
// TODO: jp - fixme!
//		FileOutputStream fos;
//		try {
//			File sdcardStorage = Environment.getExternalStorageDirectory();
//			String sdcardPath = sdcardStorage.getParent()
//					+ java.io.File.separator + sdcardStorage.getName();
//
//			File f = new File(sdcardPath + File.separator + mFileName);
//			fos = new FileOutputStream(f);
//			ObjectOutputStream os = null;
//			if(mCompressed) {
//				GZIPOutputStream gz = new GZIPOutputStream(fos);
//				os = new ObjectOutputStream(gz);
//			} else {
//				os = new ObjectOutputStream(fos);
//			}
//
//			SerializedObject3D ser = mObject.toSerializedObject3D();
//			
//			if(mObject instanceof VertexAnimationObject3D) {
//				VertexAnimationObject3D o = (VertexAnimationObject3D)mObject;
//				int numFrames = o.getNumFrames();
//				float[][] vs = new float[numFrames][];
//				float[][] ns = new float[numFrames][];
//				String[] frameNames = new String[numFrames];
//				
//				for( int i=0; i<numFrames; ++i) {
//					VertexAnimationFrame frame = (VertexAnimationFrame)o.getFrame(i);
//					AGeometry3D geom = frame.getGeometry();
//					float[] v = new float[geom.getVertices().limit()];
//					geom.getVertices().get(v);
//					float[] n = new float[geom.getNormals().limit()];
//					geom.getNormals().get(n);
//					vs[i] = v;
//					ns[i] = n;
//					frameNames[i] = frame.getName();
//				}
//				
//				ser.setFrameVertices(vs);
//				ser.setFrameNormals(ns);
//				ser.setFrameNames(frameNames);
//			}
//			
//			os.writeObject(ser);
//			os.close();
//			RajLog.i("Successfully serialized " + mFileName + " to SD card.");
//		} catch (Exception e) {
//			RajLog.e("Serializing " + mFileName + " to SD card was unsuccessfull.");
//			e.printStackTrace();
//		}
//
	}
}
