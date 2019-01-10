package ideum.com.megamovie.Java.Application;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import com.amazonaws.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimingPatch;


public class TimingDataReaderWriter {
    private FileChannel inChannel;

    private static final String dataFilePath = "integer_data_path";


    public static void StoreIntsInExernalFile(int[] ints, String path) throws IOException {
        File filePath = new File(Environment.getExternalStorageDirectory(), path);
        RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw");
        FileChannel outChannel = randomAccessFile.getChannel();


        ByteBuffer buf = ByteBuffer.allocate(ints.length * 4);
        buf.clear();
        buf.asIntBuffer().put(ints);

        outChannel.write(buf);
        outChannel.close();

    }

    public Integer getInt(int index) {
        Integer val = null;
        try {
            val = ReadIntsFromFile(index, dataFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return val;
    }


    public static void readFileFromAssets(Context context) throws IOException {
        AssetManager assetManager = context.getAssets();
        String[] files = assetManager.list("timing_files");
        String file = files[0];
        String[] parts = file.split("_");

        String contact = parts[0];
        if (!contact.equals("c1")
                && !contact.equals("c2")
                && !contact.equals("cm")
                && !contact.equals("c3")
                && !contact.equals("c4")) {
            return;
        }

        Double startingLng = -Double.valueOf(parts[1]);
        Double endingLng = -Double.valueOf(parts[2]);
        Double startingLat = Double.valueOf(parts[3]);
        Double endingLat = Double.valueOf(parts[4]);

        InputStream is = assetManager.open("timing_files/" + file);
        int[] ints = readIntsFromStream(is);

        EclipseTimingPatch patch = new EclipseTimingPatch(startingLat,endingLat,startingLng,endingLng,0.01,ints);

//        for (int i = 0; i < files.length;i++) {
//            String file = files[i];
//            InputStream inputStream = assetManager.open("timing_files/" + file);
//            intArrays.add(readIntsFromStream(inputStream));
//        }

    }

    public static int[] readIntsFromStream(InputStream inputStream) throws IOException {
        byte[] bytes = IOUtils.toByteArray(inputStream);
        inputStream.close();

        ByteBuffer bb = ByteBuffer.wrap(bytes);

        int[] ints = new int[bytes.length / 4];
        for (int i = 0; i < bytes.length / 4; i++) {
            ints[i] = bb.getInt(i * 4);
        }
        return ints;
    }


    public void readFileFromRawResources(Context context, String filePath) {
        int id = context.getResources().getIdentifier("timing_files/out_bin", "raw", context.getPackageName());
        InputStream inputStream = context.getResources().openRawResource(id);
        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            ByteBuffer bb = ByteBuffer.wrap(bytes);

            int[] ints = new int[bytes.length / 4];
            for (int i = 0; i < bytes.length / 4; i++) {
                ints[i] = bb.getInt(i * 4);
            }
            int l = ints.length;

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private Integer ReadIntsFromFile(int index, String path) throws IOException {

        if (inChannel == null) {
            File filePath = new File(Environment.getExternalStorageDirectory(), path);
            RandomAccessFile rFile = new RandomAccessFile(filePath, "rw");
            inChannel = rFile.getChannel();
        }

        if (inChannel.size() < 4 * index) {
            return null;
        }

        ByteBuffer buffIn = ByteBuffer.allocate(4);
        buffIn.clear();
        inChannel.read(buffIn, 4 * index);

        buffIn.rewind();

        return buffIn.asIntBuffer().get(0);

    }

    public void Close() {
        if (inChannel != null) {
            try {
                inChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
