package kafka.s3.consumer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.GZIPOutputStream;

import kafka.message.Message;
import kafka.message.MessageAndMetadata;

import org.apache.log4j.Logger;

class S3JsonFileSink extends S3SinkBase implements Sink {

	private static final org.apache.log4j.Logger logger = Logger.getLogger(App.class);

	private int s3MaxObjectSize;
	private long startOffset;
	private long endOffset;
	private int bytesWritten;
	ByteBuffer buffer;
	GZIPOutputStream goutStream;

	File tmpFile;
	OutputStream tmpOutputStream;
	OutputStream writer;
	String topic;

	private GZIPOutputStream getOutputStream(File tmpFile)
			throws FileNotFoundException, IOException {
		logger.info("Creating gzip output stream for tmpFile: " + tmpFile);
		return new GZIPOutputStream(new FileOutputStream(tmpFile));
	}

	public S3JsonFileSink(String topic, int partition,
			PropertyConfiguration conf) throws IOException {
		super(topic, partition, conf);

		startOffset = 0;
		endOffset = 0;
		bytesWritten = 0;
		tmpFile = File.createTempFile("s3sink", null);
		goutStream = getOutputStream(tmpFile);
		this.topic = topic;

		if (!topicSizes.containsKey(topic)) {
			logger.warn("No topic specific size found for topic: " + topic);
			s3MaxObjectSize = conf.getS3MaxObjectSize();
		} else {
			s3MaxObjectSize = topicSizes.get(topic);
		}
	}

	@Override
	public long append(MessageAndMetadata<Message> msgAndMetadata)
			throws IOException {
		int messageSize = msgAndMetadata.message().payload().remaining();
		 //logger.info("Appending message with size: " + messageSize);

		if (bytesWritten + messageSize > s3MaxObjectSize) {
			goutStream.close();
			commitChunk(tmpFile, startOffset, endOffset);
			tmpFile.delete();
			tmpFile = File.createTempFile("s3sink", null);
			goutStream.finish();
			goutStream = getOutputStream(tmpFile);
			startOffset = endOffset;
			bytesWritten = 0;
		}

		ByteBuffer buffer = msgAndMetadata.message().payload();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);

		goutStream.write(bytes);
		goutStream.write('\n');
		bytesWritten += messageSize;
		endOffset++;
		return messageSize;
	}

}
