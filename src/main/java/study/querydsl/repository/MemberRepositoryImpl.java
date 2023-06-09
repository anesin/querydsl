package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;


public class MemberRepositoryImpl implements MemberRepositoryCustom {

  private final JPAQueryFactory queryFactory;


  public MemberRepositoryImpl(EntityManager em) {
    this.queryFactory = new JPAQueryFactory(em);
  }


  @Override
  public List<MemberTeamDto> search(MemberSearchCondition condition) {
    return searchQuery(condition).fetch();
  }


  @Override
  public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
    QueryResults<MemberTeamDto> results = searchQuery(condition)
                                              .offset(pageable.getOffset())
                                              .limit(pageable.getPageSize())
                                              .fetchResults();
    return new PageImpl<>(results.getResults(), pageable, results.getTotal());
  }


  @Override
  public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
    List<MemberTeamDto> contents = searchQuery(condition)
                                       .offset(pageable.getOffset())
                                       .limit(pageable.getPageSize())
                                       .fetch();
//    long total = countQuery(condition).fetchCount();
//    return new PageImpl<>(contents, pageable, total);
    return PageableExecutionUtils.getPage(contents, pageable, countQuery(condition)::fetchCount);
  }


  private JPAQuery<MemberTeamDto> searchQuery(MemberSearchCondition condition) {
    QMemberTeamDto qMemberTeamDto = new QMemberTeamDto(member.id, member.username, member.age, team.id, team.name);
    return queryFactory.select(qMemberTeamDto)
                       .from(member)
                       .leftJoin(member.team, team)
                       .where(usernameEq(condition.getUsername()),
                              teamNameEq(condition.getTeamName()),
                              ageGoe(condition.getAgeGoe()),
                              ageLoe(condition.getAgeLoe()));
  }


  private JPAQuery<Member> countQuery(MemberSearchCondition condition) {
    return queryFactory.selectFrom(member)
                       .leftJoin(member.team, team)
                       .where(usernameEq(condition.getUsername()),
                              teamNameEq(condition.getTeamName()),
                              ageGoe(condition.getAgeGoe()),
                              ageLoe(condition.getAgeLoe()));
  }


  private BooleanExpression usernameEq(String username) {
    return hasText(username)? member.username.eq(username): null;
  }


  private BooleanExpression teamNameEq(String teamName) {
    return hasText(teamName)? team.name.eq(teamName): null;
  }


  private BooleanExpression ageGoe(Integer ageGoe) {
    return ageGoe == null? null: member.age.goe(ageGoe);
  }


  private BooleanExpression ageLoe(Integer ageLoe) {
    return ageLoe == null? null:  member.age.loe(ageLoe);
  }

}
