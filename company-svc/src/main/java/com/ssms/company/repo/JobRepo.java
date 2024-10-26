package com.ssms.company.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ssms.company.model.Job;

import java.util.List;

@Repository
public interface JobRepo extends JpaRepository<Job, String> {
    //通过团队ID查找所有Job，返回一个Job的列表
    List<Job> findJobByTeamId(String teamId);
    //通过Job的ID查找单个Job
    Job findJobById(String id);
}
