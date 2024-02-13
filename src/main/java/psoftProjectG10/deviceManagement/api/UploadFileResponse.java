package psoftProjectG10.deviceManagement.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>
 * code based on
 * https://github.com/callicoder/spring-boot-file-upload-download-rest-api-example
 *
 *
 */
@Schema(description = "Metadata about the uploaded file")
public class UploadFileResponse {

	private final String fileName;

	@Schema(description = "Absolute URL of the file")
	private final String fileDownloadUri;

	@Schema(description = "Media type")
	private final String fileType;

	@Schema(description = "File size in bytes")
	private final long size;

	public UploadFileResponse(final String fileName, final String fileDownloadUri, final String fileType,
							  final long size) {
		this.fileName = fileName;
		this.fileDownloadUri = fileDownloadUri;
		this.fileType = fileType;
		this.size = size;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFileDownloadUri() {
		return fileDownloadUri;
	}

	public String getFileType() {
		return fileType;
	}

	public long getSize() {
		return size;
	}
}