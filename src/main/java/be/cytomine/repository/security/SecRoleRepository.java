package be.cytomine.repository.security;


import be.cytomine.domain.security.SecRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the user entity.
 */
@Repository
public interface SecRoleRepository extends JpaRepository<SecRole, Long>, JpaSpecificationExecutor<SecRole> {

    default SecRole getGuest() {
        return getByAuthority("ROLE_GUEST");
    }

    default SecRole getUser() {
        return getByAuthority("ROLE_USER");
    }

    default SecRole getAdmin() {
        return getByAuthority("ROLE_ADMIN");
    }

    default SecRole getSuperAdmin() {
        return getByAuthority("ROLE_SUPER_ADMIN");
    }

    SecRole getByAuthority(String authority);

    Optional<SecRole> findByAuthority(String authority);

    default SecRole createIfNotExist(String authority) {
        return findByAuthority(authority).orElseGet(() -> {
            SecRole role = new SecRole();
            role.setAuthority(authority);
            return save(role);
        });
    }

}
