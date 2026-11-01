package com.axel.libraryapi.service;

import com.axel.libraryapi.dto.MemberRequest;
import com.axel.libraryapi.dto.MemberResponse;
import com.axel.libraryapi.entity.Loan;
import com.axel.libraryapi.entity.Member;
import com.axel.libraryapi.exception.BusinessRuleException;
import com.axel.libraryapi.exception.DuplicateResourceException;
import com.axel.libraryapi.exception.ResourceNotFoundException;
import com.axel.libraryapi.repository.LoanRepository;
import com.axel.libraryapi.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock private MemberRepository memberRepository;
    @Mock private LoanRepository loanRepository;

    @InjectMocks private MemberService memberService;

    private Member member;

    @BeforeEach
    void setUp(){
        member = Member.builder()
                .id(1L)
                .name("Ana")
                .email("ana@mail.com")
                .phone("2721234567")
                .registeredAt(LocalDate.now())
                .active(true)
                .build();
    }

    //create
    @Test
    void create_success_normalizesEmailAndActivates(){
        when(memberRepository.existsByEmail("ana@mail.com")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        MemberResponse response = memberService.create(new MemberRequest("   Ana   ", "  Ana@Mail.com  ", "2721234567"));

        assertThat(response.email()).isEqualTo("ana@mail.com");
        assertThat(response.name()).isEqualTo("Ana");
        assertThat(response.active()).isTrue();
        assertThat(response.registeredAt()).isEqualTo(LocalDate.now());
    }

    @Test
    void create_duplicateEmail_throwsDuplicate(){
        when(memberRepository.existsByEmail("ana@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> memberService.create(new MemberRequest("Ana", "ana@mail.com", "2721234567")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ana@mail.com");

        verify(memberRepository, never()).save(any());
    }

    //update
    @Test
    void update_duplicateEmail_throwsDuplicate(){
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.existsByEmailAndIdNot("luis@mail.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> memberService.update(1L, new MemberRequest("Ana", "luis@mail.com", "2721234567")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("luis@mail.com");
        verify(memberRepository, never()).save(any());
    }

    @Test
    void update_memberNotFound_throwsNotFound(){
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.update(99L, new MemberRequest("ana", "ana@mail.com", "2721234567")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Socio no encontrado");
        verify(memberRepository, never()).save(any());
    }

    //setActive
    @Test
    void setActive_deactivateWithActiveLoans_throwsBusinessRule(){
        when(loanRepository.countByMemberIdAndReturnedAtIsNull(1L)).thenReturn(1L);

        assertThatThrownBy(() -> memberService.setActive(1L, false))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No se puede desactivar");
        verify(memberRepository, never()).save(any());
    }

    @Test
    void setActive_deactivateWithoutLoans_setsInactive(){
        when(loanRepository.countByMemberIdAndReturnedAtIsNull(1L)).thenReturn(0L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        MemberResponse response = memberService.setActive(1L, false);

        assertThat(response.active()).isFalse();
    }

    @Test
    void findById_notFound_throwsNotFound(){
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Socio no encontrado");
    }
}
