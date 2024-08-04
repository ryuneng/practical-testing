package sample.cafekiosk.unit;

import lombok.Getter;
import sample.cafekiosk.unit.beverage.Beverage;
import sample.cafekiosk.unit.order.Order;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class CafeKiosk {

    public static final LocalTime SHOP_OPEN_TIME = LocalTime.of(10, 0);
    public static final LocalTime SHOP_CLOSE_TIME = LocalTime.of(22, 0);

    private final List<Beverage> beverages = new ArrayList<>();

    public void add(Beverage beverage) {
        beverages.add(beverage);
    }

    public void add(Beverage beverage, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("음료는 1잔 이상 주문하실 수 있습니다.");
        }

        for (int i = 0; i < count; i++) {
            beverages.add(beverage);
        }
    }

    public void remove(Beverage beverage) {
        beverages.remove(beverage);
    }

    public void clear() {
        beverages.clear();
    }

    public int calculateTotalPrice() {
        // TDD(Test Driven Development)
        // 1. RED: 실패하는 테스트 작성
//        return 0;

        // 2. GREEN: 테스트가 통과할 수 있는 최소한의 코딩
//        return 8500;

        // 3. REFACTOR: 테스트 통과를 유지하면서 구현 코드 개선
//        int totalPrice = 0;
//        for (Beverage beverage : beverages) {
//            totalPrice += beverage.getPrice();
//        }
//        return totalPrice;

        return beverages.stream()
                .mapToInt(Beverage::getPrice)
                .sum();
    }

    public Order createOrder(LocalDateTime currentDateTime) { // 2. 수정된 currentDateTime - 외부에서 파라미터로 받도록 설정하여 시간을 유연하게 지정할 수 있음
        // 1. 기존 currentDateTime - 현재 날짜/시간은 실제 테스트를 수행하는 시간에 따라 테스트 결과가 달라질 수 있음
//        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalTime currentTime = currentDateTime.toLocalTime();
        if (currentTime.isBefore(SHOP_OPEN_TIME) || currentTime.isAfter(SHOP_CLOSE_TIME)) {
            throw new IllegalArgumentException("주문 시간이 아닙니다. 관리자에게 문의하세요.");
        }
        
        return new Order(currentDateTime, beverages);
    }

}
