package com.mikelau.croperino;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

public class InternalStorageContentProvider extends ContentProvider {
    public static final Uri CONTENT_URI = Uri.parse("content://com.mikelau.croperino/");
	private static final HashMap<String, String> MIME_TYPES = new HashMap<>();
	
	static {
		MIME_TYPES.put(".jpg", "image/jpeg");
		MIME_TYPES.put(".jpeg", "image/jpeg");
	}

	@Override
	public boolean onCreate() {
		try {
			File mFile = new File(getContext().getFilesDir(), CroperinoConfig.getsImageName());
			if(!mFile.exists()) {
				mFile.createNewFile();
				getContext().getContentResolver().notifyChange(CONTENT_URI, null);
			}
			return (true);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public String getType(Uri uri) {
		String path = uri.toString();
		for (String extension : MIME_TYPES.keySet()) {
			if (path.endsWith(extension)) {
				return (MIME_TYPES.get(extension));
			}
		}
		return (null);
	}
	
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		File f = new File(getContext().getFilesDir(), CroperinoConfig.getsImageName());
		if (f.exists()) {
			return (ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_WRITE));
		}
		throw new FileNotFoundException(uri.getPath());
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		return null;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}
}