package sample.cafekiosk.spring.domain.stock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;


class StockTest {

    // ++ 아래와 같은 공유자원은 사용하지 말자. (2가지 이상의 테스트가 1개의 자원을 공유하는 것)
    //    공유자원으로 인해 테스트에 순서가 생겨 테스트 간 순서에 따라 성공/실패가 판가름날 수 있기 때문이다.
    //    모든 테스트는 각각 독립적으로 언제 수행되든 항상 같은 결과가 나오는 것이 올바른 테스트 코드라고 할 수 있다.
    private static final Stock stock = Stock.create("001", 1);

    @DisplayName("현재고 수량이 요청하는 재고 수량보다 적은지 확인한다.")
    @Test
    void isQuantityLessThan() {
        // given
//        Stock stock = Stock.create("001", 1);
        int quantity = 2;

        // when
        boolean result = stock.isQuantityLessThan(quantity);

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("재고를 주어진 개수만큼 차감할 수 있다.")
    @Test
    void deductQuantity() {
        // given
//        Stock stock = Stock.create("001", 1);
        int quantity = 1;

        // when
        stock.deductQuantity(quantity);

        // then
        assertThat(stock.getQuantity()).isZero();
    }

    @DisplayName("재고보다 많은 수의 수량으로 차감을 시도하는 경우 예외가 발생한다.")
    @Test
    void deductQuantity2() {
        // given
//        Stock stock = Stock.create("001", 1);
        int quantity = 2;

        // when // then
        assertThatThrownBy(() -> stock.deductQuantity(quantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("차감할 재고 수량이 없습니다.");
    }

}