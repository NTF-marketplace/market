-- listing(sell) - 가격, nftId, address, endDate , createdDate, 활성화
-- buy - (동시성 이슈) 낙관적락으로 구현, listing과 1:1매핑, address, createdDate <큐에 담아 놓는건 어떤가?, 우선순위 주문 롤백시 다음 주문 처리>
-- nft -
    -- 구매 흐름 (트랜잭션 사가 적용)
    -- account 에 입금 ( 잔액 에러시 롤백)
    -- accountNft에 전달
-- match -구매흐름 완료 후 체결되면 lstingId 과 buyId, createdDate

--

