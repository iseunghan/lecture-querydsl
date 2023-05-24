package me.iseunghan.lecturequerydsl.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * me.iseunghan.lecturequerydsl.dto.QMemberQueryDslDto is a Querydsl Projection type for MemberQueryDslDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QMemberQueryDslDto extends ConstructorExpression<MemberQueryDslDto> {

    private static final long serialVersionUID = -89502204L;

    public QMemberQueryDslDto(com.querydsl.core.types.Expression<String> username, com.querydsl.core.types.Expression<Integer> age) {
        super(MemberQueryDslDto.class, new Class<?>[]{String.class, int.class}, username, age);
    }

}

