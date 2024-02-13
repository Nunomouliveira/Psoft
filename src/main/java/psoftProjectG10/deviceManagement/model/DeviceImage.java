package psoftProjectG10.deviceManagement.model;

import javax.persistence.*;

@Entity
public class DeviceImage {
	@Id
	@GeneratedValue
	private Long pk;

	@ManyToOne
	private Device device;

	@Lob
	private byte[] image;

	private String contentType;
}
