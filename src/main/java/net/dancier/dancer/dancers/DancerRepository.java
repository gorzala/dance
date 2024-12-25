package net.dancier.dancer.dancers;

import net.dancier.dancer.core.model.Dancer;
import net.dancier.dancer.core.model.Gender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DancerRepository extends JpaRepository<Dancer, UUID> {

    Optional<Dancer> findByUserId(UUID userid);

    Boolean existsByDancerName(String dancerName);

    List<Dancer> findFirst500ByGenderAndLongitudeBetweenAndLatitudeBetween(
            Gender gender,
            double lowerLongitude,
            double upperLongitude,
            double lowerLatitude,
            double upperLatitude
    );

}
