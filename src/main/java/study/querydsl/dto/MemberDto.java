package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@NoArgsConstructor
@Setter
@ToString
public class MemberDto {

  private String username;
  private int age;


  @QueryProjection  // dto 에 있어 의존성 문제를 생각해야 한다.
  public MemberDto(String username, int age) {
    this.username = username;
    this.age = age;
  }

}
