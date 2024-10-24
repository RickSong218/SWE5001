package com.ssms.company.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.ssms.company.model.Shift;

import java.time.Instant;
import java.util.List;

@Repository
public interface ShiftRepo extends JpaRepository<Shift, String> {
    // 根据班次ID查找班次
    Shift findShiftById(String shiftId);
    // 删除班次的查询，通过班次ID删除指定的班次
    @Modifying(clearAutomatically = true)
    @Query("delete from Shift shift where shift.id = :shiftId")
    @Transactional
    int deleteShiftById(@Param("shiftId") String shiftId);
    // 获取每周排班情况的统计信息，使用原生SQL查询
    @Query(
            value = "select cast(a.weekname as char) as week, greatest(a.count, coalesce(b.count,0)) as count from (select 0 as count, str_to_date(concat(year(start), week(start), ' Monday'), '%X%V %W') as weekname from shift where start < NOW() group by weekname) as a left join (select count(distinct(user_id)) as count, str_to_date(concat(year(start), week(start), ' Monday'), '%X%V %W') as weekname from shift where start < NOW() and user_id != '' and published is true group by weekname) as b on a.weekname = b.weekname",
            nativeQuery = true
    )
    List<IScheduledPerWeek> getScheduledPerWeekList(); // 返回每周排班信息的接口
    // 定义接口IScheduledPerWeek，表示每周排班的统计数据
    interface IScheduledPerWeek {
        String getWeek();// 获取周信息
        int getCount();// 获取每周的排班数量

    }
    // 获取当前正在值班的人员数量
    @Query(
            value = "select count(distinct(user_id)) from shift where shift.start <= NOW() and shift.stop > NOW() and user_id <> '' and shift.published = true",
            nativeQuery = true
    )
    int getPeopleOnShifts();// 返回正在工作的人员数量

    // 列出某个团队中某个用户在指定时间范围内的班次，按开始时间升序排列
    @Query(
            value = "select shift from Shift shift where shift.teamId = :teamId and shift.userId = :userId and shift.start >= :startTime and shift.start < :endTime order by shift.start asc"
    )
    List<Shift> listWorkerShifts(@Param("teamId") String teamId, @Param("userId") String userId, @Param("startTime") Instant start, @Param("endTime") Instant end);
    // 列出某个团队中某个用户在指定时间范围内的班次，不排序
    @Query(
            value = "select shift from Shift shift where shift.teamId = :teamId and shift.userId = :userId and shift.start >= :startTime and shift.start < :endTime"
    )
    List<Shift> listShiftByUserId(@Param("teamId") String teamId, @Param("userId") String userId, @Param("startTime") Instant start, @Param("endTime") Instant end);
    // 列出某个团队中某个职位在指定时间范围内的班次
    @Query(
            value = "select shift from Shift shift where shift.teamId = :teamId and shift.jobId = :jobId and shift.start >= :startTime and shift.start < :endTime"
    )
    List<Shift> listShiftByJobId(@Param("teamId") String teamId, @Param("jobId") String jobId, @Param("startTime") Instant start, @Param("endTime") Instant end);
    // 列出某个团队中某个用户和职位的班次
    @Query(
            value = "select shift from Shift shift where shift.teamId = :teamId and shift.userId = :userId and shift.jobId = :jobId and shift.start >= :startTime and shift.start < :endTime"
    )
    List<Shift> listShiftByUserIdAndJobId(@Param("teamId") String teamId, @Param("userId") String userId, @Param("jobId") String jobId, @Param("startTime") Instant start, @Param("endTime") Instant end);
    // 列出某个团队中指定时间范围内的所有班次
    @Query(
            value = "select shift from Shift shift where shift.teamId = :teamId and shift.start >= :startTime and shift.start < :endTime"
    )
    List<Shift> listShiftByTeamIdOnly(@Param("teamId") String teamId, @Param("startTime") Instant start, @Param("endTime") Instant end);
}
