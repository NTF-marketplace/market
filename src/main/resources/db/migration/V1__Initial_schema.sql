-- listing(sell) - 가격, nftId, address, endDate , createdDate, 활성화
-- offer(buy)
-- Order(buy,sell) - (동시성 이슈) 낙관적락으로 구현, listing과 1:1매핑, address, createdDate <큐에 담아 놓는건 어떤가?, 우선순위 주문 롤백시 다음 주문 처리>
-- nft -
    -- 구매 흐름 (트랜잭션 사가 적용)
    -- account 에 입금 ( 잔액 에러시 롤백)
    -- accountNft에 전달
-- match -구매흐름 완료 후 체결되면 lstingId 과 buyId, createdDate

CREATE TYPE  chain_type AS ENUM (
    'ETHEREUM_MAINNET',
    'LINEA_MAINNET',
    'LINEA_SEPOLIA',
    'POLYGON_MAINNET',
    'ETHEREUM_HOLESKY',
    'ETHEREUM_SEPOLIA',
    'POLYGON_AMOY'
    );

CREATE TYPE token_type AS ENUM (
    'MATIC',
    'BTC',
    'ETH'
    );


CREATE TABLE IF NOT EXISTS nft (
    id BIGINT PRIMARY KEY,
    token_id VARCHAR(255) NOT NULL,
    token_address VARCHAR(255) NOT NULL,
    chain_type chain_type NOT NULL
    );


CREATE TABLE IF NOT EXISTS listing (
    id SERIAL PRIMARY KEY,
    nft_id BIGINT REFERENCES nft(id),
    address VARCHAR(255) NOT NULL,
    created_at BIGINT not null,
    end_date BIGINT not null,
    active bool not null,
    price DECIMAL(19, 4) NOT NULL,
    token_type token_type not null
);