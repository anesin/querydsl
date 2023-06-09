package study.querydsl.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;


@Entity
@Getter
@Setter
@NoArgsConstructor(access = PROTECTED)
@ToString(of = {"id", "username", "age"})
public class Member {

  @Id
  @GeneratedValue
  @Column(name = "member_id")
  private Long id;
  private String username;
  private int age;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "team_id")
  private Team team;


  public Member(String username) {
    this(username, 0);
  }


  public Member(String username, int age) {
    this(username, age, null);
  }


  public Member(String username, int age, Team team) {
    this.age = age;
    this.username = username;
    changeTeam(team);
  }


  public void changeTeam(Team team) {
    if (team != null) {
      this.team = team;
      team.getMembers().add(this);
    }
  }

}
