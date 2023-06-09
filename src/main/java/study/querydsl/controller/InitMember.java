package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;


@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {

  @Component
  public static class InitMemberService {
    @PersistenceContext
    EntityManager em;

    @Transactional
    public void init() {
      Team teamA = new Team("teamA");
      Team teamB = new Team("teamB");
      em.persist(teamA);
      em.persist(teamB);

      for (int i = 0; i < 100; ++i)
        em.persist(new Member("member" + i, i, (i & 1) == 0? teamA: teamB));
    }
  }


  private final InitMemberService initMemberService;


  @PostConstruct
  public void init() {
    initMemberService.init();
  }

}
