package net.dancier.dancer.core.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "dancer")
public class Dancer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID userId;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "able_to",
            joinColumns = @JoinColumn(name = "dancer_id"),
            inverseJoinColumns = @JoinColumn(name = "dance_profile_id"))
    private Set<DanceProfile> ableTo = new HashSet<>();

    @OneToMany
    @JoinTable(name = "wants_to",
            joinColumns = @JoinColumn(name = "dancer_id"),
            inverseJoinColumns = @JoinColumn(name = "dance_profile_id"))
    private Set<DanceProfile> wantsTo = new HashSet<>();

    private Integer size;

    @Column(name = "birth_date")
    @Temporal(TemporalType.DATE)
    private Date birthDate;

    @Enumerated(EnumType.STRING)
    private Sex sex;

}
