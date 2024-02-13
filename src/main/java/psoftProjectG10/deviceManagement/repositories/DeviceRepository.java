package psoftProjectG10.deviceManagement.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import psoftProjectG10.deviceManagement.model.Device;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends CrudRepository<Device, Long> {
    Optional<Device> findByMacAddress(String macAddress);

    Device getByMacAddress(String macAddress);

    @Query("select d from Device d where d.subscription.idSub = :idSub")
    List<Device> searchByIdSub(@Param("idSub") Long idSub);

    @Query("SELECT s.idSub FROM Subscription s WHERE s.user.id = :userId")
    Long findSubscriptionIdByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Device d WHERE d.macAddress = ?1 AND d.version = ?2")
    int deleteByIDIfMatch(String macAddress, long desiredVersion);

    @Query("SELECT COUNT(d) FROM Device d WHERE d.subscription.idSub = :idSub")
    int countDevicesBySubscriptionId(@Param("idSub") Long idSub);

    @Query("select d from Device d ")
    Page<Device> findAllDevices(Pageable pageable);


}