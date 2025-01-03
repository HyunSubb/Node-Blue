package com.samsa.core.port;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import com.samsa.core.Message;
import com.samsa.core.Pipe;
import lombok.extern.slf4j.Slf4j;

/**
 * InPort 클래스는 노드의 입력 포트를 구현합니다.
 * 여러 파이프로부터 메시지를 수신하고 관리하는 기능을 제공합니다.
 * 
 * <p>
 * 주요 기능:
 * </p>
 * <ul>
 * <li>다중 입력 파이프 지원: 여러 소스로부터 데이터 수신 가능</li>
 * <li>메시지 수신 및 소비: 연결된 파이프로부터 순차적으로 메시지 처리</li>
 * <li>파이프 동적 추가/제거: 런타임에 입력 소스 변경 가능</li>
 * <li>데이터 가용성 확인: 처리 가능한 메시지 존재 여부 확인</li>
 * </ul>
 *
 * <p>
 * 사용 예시:
 * </p>
 * 
 * <pre>
 * InPort port = new InPort();
 * port.addPipe(sourcePipe);
 * if (port.hasAvailableData()) {
 *     Message message = port.consume();
 *     // 메시지 처리
 * }
 * </pre>
 *
 * @author samsa
 * @version 1.0
 * @see Pipe
 * @see Message
 */
@Slf4j
public class InPort {
    /** 포트의 고유 식별자 */
    private UUID id;

    /** 입력 파이프들의 목록 */
    private List<Pipe> pipes;

    /**
     * 기본 생성자. 새로운 UUID로 포트를 생성합니다.
     */
    public InPort() {
        this(UUID.randomUUID());
    }

    /**
     * 지정된 ID로 포트를 생성합니다.
     *
     * @param id 포트의 고유 식별자
     */
    public InPort(UUID id) {
        this.id = id;
        pipes = new ArrayList<>();
    }

    /**
     * 연결된 파이프들로부터 메시지를 소비합니다.
     * 파이프들을 순차적으로 확인하여 첫 번째로 발견된 메시지를 반환합니다.
     *
     * @return 수신된 메시지, 또는 가용한 메시지가 없는 경우 null
     */
    public Message consume() {
        log.info("메세지 생김");
        for (Pipe pipe : pipes) {
            if (Objects.isNull(pipe)) {
                continue;
            }
            Message message = pipe.poll();
            if (Objects.nonNull(message)) {
                return message;
            }
        }

        return null;
    }

    /**
     * 처리 가능한 데이터의 존재 여부를 확인합니다.
     *
     * @return 하나 이상의 파이프에 메시지가 있으면 true, 그렇지 않으면 false
     */
    public boolean hasAvailableData() {
        return pipes.stream().anyMatch(pipe -> Objects.nonNull(pipe) && !pipe.isEmpty());
    }

    /**
     * 새로운 입력 파이프를 추가합니다.
     *
     * @param pipe 추가할 파이프
     * @throws IllegalArgumentException pipe가 null인 경우
     */
    public void addPipe(Pipe pipe) {
        if (Objects.isNull(pipe)) {
            throw new IllegalArgumentException("Pipe cannot be null");
        }
        pipes.add(pipe);
    }

    /**
     * 기존 입력 파이프를 제거합니다.
     *
     * @param pipe 제거할 파이프
     * @throws IllegalArgumentException pipe가 null인 경우
     */
    public void removePipe(Pipe pipe) {
        if (Objects.isNull(pipe)) {
            throw new IllegalArgumentException("Pipe cannot be null");
        }
        pipes.remove(pipe);
    }

    /**
     * 포트의 고유 식별자를 반환합니다.
     *
     * @return 포트의 UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * 현재 연결된 모든 파이프의 목록을 반환합니다.
     *
     * @return 파이프 목록
     */
    public List<Pipe> getPipes() {
        return pipes;
    }
}
