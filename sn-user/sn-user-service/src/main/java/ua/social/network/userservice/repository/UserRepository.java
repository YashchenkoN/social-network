package ua.social.network.userservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ua.social.network.userservice.entity.Role;
import ua.social.network.userservice.entity.User;

/**
 * @author Mykola Yashchenko
 */
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    @Modifying
    @Query("update User u set u.role = :roleType where u.id = :userId")
    int updateRole(@Param("userId") String userId, @Param("roleType") Role roleType);

    @Modifying
    @Query("update User u set u.name = :name where u.id = :userId")
    int modify(@Param("userId") String userId, @Param("name") String avatarId);

    @Modifying
    @Query(nativeQuery = true,
            value = "update users set users.profile_picture_id = :profilePictureId where users.id = :userId")
    int updateAvatar(@Param("userId") String userId, @Param("profilePictureId") String profilePictureId);
}
