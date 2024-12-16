// package com.samsa.node.out;

// import java.util.HashSet;
// import java.util.Map;
// import java.util.Set;
// import java.util.UUID;
// import java.util.HashMap;
// import java.io.PrintWriter;
// import java.io.StringWriter;

// import com.samsa.core.Message;
// import com.samsa.core.OutNode;
// import com.samsa.core.OutPort;
// import com.samsa.core.Pipe;

// import lombok.Getter;
// import lombok.Setter;
// import lombok.extern.slf4j.Slf4j;

// /**
// * 노드 실행 중 발생하는 에러를 처리하는 노드입니다.
// * 두 가지 모드로 작동할 수 있습니다:
// * 1. 같은 파이프라인 내의 모든 노드의 에러를 처리
// * 2. 선택된 특정 노드들의 에러만 처리
// *
// * 에러가 발생하면 에러 정보를 포함한 메시지를 생성하여 연결된 다음 노드로 전달합니다.
// */
// @Slf4j
// public class CatchNode extends OutNode {

// /**
// * 에러를 캐치할 범위를 정의하는 열거형입니다.
// */
// public enum CatchScope {
// /** 같은 파이프라인에 있는 모든 노드의 에러를 처리합니다. */
// SAME_PIPELINE,
// /** 사용자가 선택한 특정 노드들의 에러만 처리합니다. */
// SELECTED_NODES
// }

// /** 현재 설정된 에러 캐치 범위 */
// @Getter
// @Setter
// private CatchScope scope = CatchScope.SAME_PIPELINE;

// /** 이 노드가 속한 파이프라인의 ID */
// @Getter
// @Setter
// private String pipelineId;

// /** 에러를 캐��할 대상 노드들의 ID 집합 */
// private final Set<String> targetNodeIds = new HashSet<>();

// /**
// * CatchNode를 생성합니다.
// *
// * @param id 노드의 고유 식별자 (UUID 형식의 문자열)
// * @param scope 에러 캐치 범위 (SAME_PIPELINE 또는 SELECTED_NODES)
// * @throws IllegalArgumentException id가 유효한 UUID 형식이 아닌 경우
// */
// public CatchNode(String id, CatchScope scope) {
// super(new OutPort(UUID.fromString(id), null));
// this.scope = scope;
// }

// /**
// * 에러를 캐치할 대상 노드를 추가합니다.
// * scope가 SELECTED_NODES인 경우에만 의미가 있습니다.
// *
// * @param nodeId 에러를 캐치할 노드의 ID
// */
// public void addTargetNode(String nodeId) {
// targetNodeIds.add(nodeId);
// }

// /**
// * 에러 캐치 대상에서 특정 노드를 제거합니다.
// *
// * @param nodeId 제거할 노드의 ID
// */
// public void removeTargetNode(String nodeId) {
// targetNodeIds.remove(nodeId);
// }

// /**
// * 주어진 노드의 에러를 처리할 수 있는지 확인합니다.
// *
// * @param sourceNodeId 에러가 발생한 노드의 ID
// * @param sourcePipelineId 에러가 발생한 노드가 속한 파이프라인의 ID
// * @return 에러를 처리할 수 있으면 true, 그렇지 않으면 false
// */
// public boolean canHandleError(String sourceNodeId, String sourcePipelineId) {
// return switch (scope) {
// case SAME_PIPELINE -> pipelineId.equals(sourcePipelineId);
// case SELECTED_NODES -> targetNodeIds.contains(sourceNodeId);
// };
// }

// /**
// * 발생한 에러를 처리합니다.
// * 에러 정보를 포함한 메시지를 생성하여 연결된 다음 노드로 전달합니다.
// *
// * @param sourceNodeId 에러가 발생한 노드의 ID
// * @param error 발생한 에러 객체
// * @throws IllegalArgumentException sourceNodeId가 null이거나 빈 문자열인 경우, 또는 error가
// * null인 경우
// */
// public void handleNodeError(String sourceNodeId, Throwable error) {
// if (sourceNodeId == null || sourceNodeId.trim().isEmpty()) {
// throw new IllegalArgumentException("Source node ID cannot be null or empty");
// }
// if (error == null) {
// throw new IllegalArgumentException("Error cannot be null");
// }

// try {
// // 에러 정보를 메타데이터로 구성
// Map<String, Object> errorInfo = new HashMap<>();
// errorInfo.put("error", error.getMessage());
// errorInfo.put("sourceNode", sourceNodeId);
// errorInfo.put("timestamp", System.currentTimeMillis());
// errorInfo.put("errorType", error.getClass().getName());

// // 스택트레이스 추가
// StringWriter sw = new StringWriter();
// error.printStackTrace(new PrintWriter(sw));
// errorInfo.put("stackTrace", sw.toString());

// // 에러 메시지 생성 및 전송
// Message errorMessage = new Message(error, errorInfo);
// emit(errorMessage); // OutNode의 emit 메서드 사용

// log.error("Caught error from Node[{}]: {}", sourceNodeId,
// error.getMessage());

// } catch (Exception e) {
// log.error("Failed to handle error from Node[{}]", sourceNodeId, e);
// handleError(e);
// throw new IllegalStateException("Failed to process error message", e);
// }
// }

// /**
// * CatchNode 자체에서 발생한 에러를 처리합니다.
// *
// * @param error 발생한 에러 객체
// */
// @Override
// public void handleError(Throwable error) {
// super.handleError(error);
// log.error("Error in CatchNode[{}]: {}", getId(), error.getMessage());
// }
// }