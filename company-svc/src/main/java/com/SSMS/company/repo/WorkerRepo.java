package com.SSMS.company.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.SSMS.company.model.Worker;

import java.util.List;

@Repository
public interface WorkerRepo extends JpaRepository<Worker, String> {
    // 根据团队ID查找所有属于该团队的Worker
    List<Worker> findByTeamId(String teamId);
    // 根据用户ID查找所有属于该用户的Worker
    List<Worker> findByUserId(String userId);
    // 根据团队ID和用户ID查找具体的Worker
    Worker findByTeamIdAndUserId(String teamId, String userId);
    //通过团队ID和用户ID删除指定的Worker
    @Modifying(clearAutomatically = true)
    @Query("delete from Worker worker where worker.teamId = :teamId and worker.userId = :userId")
    @Transactional
    int deleteWorker(@Param("teamId") String teamId, @Param("userId") String userId);
}
