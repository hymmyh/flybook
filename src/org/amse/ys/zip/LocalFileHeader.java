package org.amse.ys.zip;

/**
 * Class consists of constants, describing a compressed file. Contains only
 * construcor, all fields are final.
 */

import java.io.IOException;

public class LocalFileHeader {
    static final int FILE_HEADER_SIGNATURE = 0x04034b50;
    static final int FOLDER_HEADER_SIGNATURE = 0x02014b50;
    static final int END_OF_CENTRAL_DIRECTORY_SIGNATURE = 0x06054b50;
    static final int DATA_DESCRIPTOR_SIGNATURE = 0x08074b50;

	int Signature;

    int Version;
	int Flags;
    int CompressionMethod;
	int ModificationTime;
	int ModificationDate;
	int CRC32;
    int CompressedSize;
    int UncompressedSize;
	int NameLength;
	int ExtraLength;

	public String FileName;
	int DataOffset;

    LocalFileHeader() {
    }

    void readFrom(MyBufferedInputStream stream,boolean flag) throws IOException {
		Signature = stream.read4Bytes();
		switch (Signature) {
			default:
				break;
			case END_OF_CENTRAL_DIRECTORY_SIGNATURE:
			{
				stream.skip(16);
                int comment = stream.read2Bytes();
				stream.skip(comment);
				break;
			}
			case FOLDER_HEADER_SIGNATURE://zip 文件
			{
                Version = stream.read4Bytes();
                Flags = stream.read2Bytes();
                CompressionMethod = stream.read2Bytes();
                ModificationTime = stream.read2Bytes();
                ModificationDate = stream.read2Bytes();
                CRC32 = stream.read4Bytes();
                CompressedSize = stream.read4Bytes();
                UncompressedSize = stream.read4Bytes();
				if (CompressionMethod == 0 && CompressedSize != UncompressedSize) {
					CompressedSize = UncompressedSize;
				}
                NameLength = stream.read2Bytes();
                ExtraLength = stream.read2Bytes();
                int comment = stream.read2Bytes();
				stream.skip(12);
//				FileName = stream.readString(NameLength);
				//hym 加，为了处理 文件名是中文的问题,导致 epub 慢的很
				if(flag){
					FileName = stream.readString(NameLength,true);
				}else{
					FileName = stream.readString(NameLength);
				}
				stream.skip(ExtraLength);
				stream.skip(comment);
				break;
			}
			case FILE_HEADER_SIGNATURE://epub 文件
                Version = stream.read2Bytes();
                Flags = stream.read2Bytes();
                CompressionMethod = stream.read2Bytes();
                ModificationTime = stream.read2Bytes();
                ModificationDate = stream.read2Bytes();
                CRC32 = stream.read4Bytes();
                CompressedSize = stream.read4Bytes();
                UncompressedSize = stream.read4Bytes();
				if (CompressionMethod == 0 && CompressedSize != UncompressedSize) {
					CompressedSize = UncompressedSize;
				}
                NameLength = stream.read2Bytes();
                ExtraLength = stream.read2Bytes();
                if(flag){
					FileName = stream.readString(NameLength,true);
				}else{
					FileName = stream.readString(NameLength);
				}
//				FileName = stream.readString(NameLength);
//                FileName = stream.readString(NameLength,true);
				stream.skip(ExtraLength);
				break;
			case DATA_DESCRIPTOR_SIGNATURE:
				CRC32 = stream.read4Bytes();
				CompressedSize = stream.read4Bytes();
				UncompressedSize = stream.read4Bytes();
				break;
		}
		DataOffset = stream.offset();
    }
}
