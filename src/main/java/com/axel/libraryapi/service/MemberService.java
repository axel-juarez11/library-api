package com.axel.libraryapi.service;

import com.axel.libraryapi.dto.MemberRequest;
import com.axel.libraryapi.dto.MemberResponse;
import com.axel.libraryapi.entity.Member;
import com.axel.libraryapi.exception.DuplicateResourceException;
import com.axel.libraryapi.exception.ResourceNotFoundException;
import com.axel.libraryapi.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.metamodel.model.domain.internal.MapMember;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    @Transactional
    public MemberResponse create(MemberRequest request){
        String email = normalize(request.email());
        if (memberRepository.existsByEmail(email)){
            throw new DuplicateResourceException("Ya existe un socio con el correo " + email);
        }
        Member member = Member.builder()
                .name(request.name().trim())
                .email(email)
                .phone(request.phone())
                .registeredAt(LocalDate.now())
                .active(true)
                .build();

        return MemberResponse.from(memberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAll(Boolean active){
        List<Member> members = (active == null)
                ? memberRepository.findAll()
                : memberRepository.findByActive(active);
        return members.stream().map(MemberResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MemberResponse findById(Long id){
        return MemberResponse.from(getMember(id));
    }

    @Transactional
    public MemberResponse update(Long id, MemberRequest request){
        Member member = getMember(id);
        String email = normalize(request.email());
        if (memberRepository.existsByEmailAndIdNOt(email, id)){
            throw new DuplicateResourceException("Ya existe un socio con el correo " + email);
        }
        member.setName(request.name().trim());
        member.setEmail(email);
        member.setPhone(request.phone());
        return MemberResponse.from(memberRepository.save(member));
    }

    @Transactional
    public MemberResponse setActive(Long id, boolean active){
        Member member = getMember(id);
        member.setActive(active);
        return MemberResponse.from(memberRepository.save(member));
    }

    private Member getMember(Long id){
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Socio no encontrado con id " + id));
    }

    private String normalize(String email){
        return email.trim().toLowerCase();
    }
}
