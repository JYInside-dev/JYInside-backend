package org.najeungyoon.communitybackend.repository;

import org.najeungyoon.communitybackend.domain.app.user.UserWarning;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserWarningRepository extends JpaRepository<UserWarning,Long> {
}
