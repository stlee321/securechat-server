# Secure Chat Server

STOMP over WebSocket 채팅 서버입니다.

STOMP 플러그인이 활성화된 rabbitmq를 브로커로 이용합니다.

채팅 메세지는 AWS DynamoDB에 저장됩니다.

전송된 이미지는 AWS S3에 저장됩니다.

모든 채팅 메세지와 이미지는 클라이언트 사이드에서 암호화 되므로 서버에서 내용을 읽을 수 없습니다.