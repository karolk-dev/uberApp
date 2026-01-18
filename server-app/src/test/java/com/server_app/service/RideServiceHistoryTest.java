//package com.server_app.service;
//
//import com.querydsl.core.BooleanBuilder;
//import com.querydsl.core.types.Expression;
//import com.querydsl.core.types.Predicate;
//import com.querydsl.jpa.impl.JPAQuery;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import com.server_app.dto.RideHistorySearchCriteria;
//import com.server_app.model.QRide;
//import com.server_app.repository.RideRepository;
//import com.uber.common.dto.RideHistoryDto;
//import jakarta.persistence.EntityManager;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.ArgumentMatchers;
//import org.mockito.Captor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.modelmapper.ModelMapper;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//
//import java.math.BigDecimal;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class RideServiceHistoryTest {
//    @Mock
//    private RideRepository rideRepository;
//    @Mock
//    private ModelMapper modelMapper;
//    @Mock
//    private PaymentService paymentService;
//    @Mock
//    private EmailService emailService;
//
//    // --- Mocks CRUCIAL for testing QueryDSL methods ---
//    @Mock
//    private EntityManager entityManager; // Mocked EntityManager
//    @Mock
//    private JPAQueryFactory queryFactory; // Mocked QueryFactory
//    @Mock
//    private JPAQuery<RideHistoryDto> jpaQuery; // Mocked Query object
//    // --- End of crucial mocks ---
//
//    @InjectMocks // Inject all mocks into RideService
//    private RideService rideService;
//
//    // Captor to verify the predicate passed to QueryDSL
//    @Captor
//    private ArgumentCaptor<Predicate> predicateCaptor;
//    @Captor
//    private ArgumentCaptor<Long> offsetCaptor;
//    @Captor
//    private ArgumentCaptor<Long> limitCaptor;
//
//
//    private String testDriverUuid;
//    private String testClientUuid;
//    private RideHistorySearchCriteria searchCriteria;
//    private PageRequest pageRequest;
//    private List<RideHistoryDto> expectedRideDtos;
//
//    @BeforeEach
//    void setUp() {
//        testDriverUuid = "driver-uuid-123";
//        testClientUuid = "client-uuid-456";
//        pageRequest = PageRequest.of(0, 10); // First page, 10 items
//
//        // Reset criteria for each test
//        searchCriteria = RideHistorySearchCriteria.builder().build();
//
//        // Sample data returned by the mocked query
//        RideHistoryDto ride1 = RideHistoryDto.builder().rideUuid("ride-1").amount(new BigDecimal("10.00")).build();
//        RideHistoryDto ride2 = RideHistoryDto.builder().rideUuid("ride-2").amount(new BigDecimal("15.50")).build();
//        expectedRideDtos = Arrays.asList(ride1, ride2);
//
//        // --- Mocking the QueryDSL execution chain ---
//        // IMPORTANT: This setup assumes `rideService` uses the mocked `queryFactory`.
//        // If `new JPAQueryFactory(entityManager)` is used directly in the methods,
//        // this mocking needs adjustment (e.g., PowerMockito or refactoring).
//        // We will proceed assuming the factory mock works for the unit test.
//
//        // When queryFactory.select(...) is called, return the mocked jpaQuery
//        // Use lenient() because not all tests might call select (though they should)
//        // and to avoid UnnecessaryStubbingException if the factory isn't used as expected.
//        // Consider removing lenient() if the factory interaction is guaranteed and strict stubbing is desired.
//        lenient().when(queryFactory.select(ArgumentMatchers.<Expression<RideHistoryDto>>any())).thenReturn(jpaQuery);
//
//        // Chain the mock calls for jpaQuery
//        when(jpaQuery.from(any(QRide.class))).thenReturn(jpaQuery);
//        when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery); // Capture predicate here
//        when(jpaQuery.offset(anyLong())).thenReturn(jpaQuery);        // Capture offset
//        when(jpaQuery.limit(anyLong())).thenReturn(jpaQuery);         // Capture limit
//        when(jpaQuery.fetch()).thenReturn(expectedRideDtos);          // Return sample data on fetch()
//        when(jpaQuery.fetchCount()).thenReturn((long) expectedRideDtos.size()); // Return count on fetchCount()
//
//        // Associate the mocked queryFactory with the mocked entityManager IF NEEDED
//        // This line might be redundant if @InjectMocks handles it, or if the internal
//        // creation `new JPAQueryFactory(entityManager)` cannot be intercepted easily.
//        // For this test setup to work ideally, RideService should obtain JPAQueryFactory
//        // in a mockable way (e.g., injection or a protected factory method).
//        // We simulate that the service somehow uses our mocked queryFactory.
//        // A common pattern (if not injected) is a helper method:
//        // protected JPAQueryFactory getQueryFactory() { return new JPAQueryFactory(entityManager); }
//        // Then this method could be overridden or mocked.
//
//        // *Assume* for the test that the service gets the mocked factory.
//        // If testing the *actual* code without refactoring: you might need PowerMockito
//        // to mock `new JPAQueryFactory(entityManager)` to return `this.queryFactory`.
//    }
//
//    @Test
//    @DisplayName("getDriverRideHistory - No criteria - Success")
//    void getDriverRideHistory_NoCriteria_ReturnsPage() {
//        // Arrange
//        long expectedTotal = expectedRideDtos.size();
//        // Reset mock interactions before the Act phase for clarity in verification
//        Mockito.reset(jpaQuery); // Reset interactions on the query mock specifically
//        // Re-apply basic query mock behavior after reset
//        when(jpaQuery.from(any(QRide.class))).thenReturn(jpaQuery);
//        when(jpaQuery.where(predicateCaptor.capture())).thenReturn(jpaQuery);
//        when(jpaQuery.offset(offsetCaptor.capture())).thenReturn(jpaQuery);
//        when(jpaQuery.limit(limitCaptor.capture())).thenReturn(jpaQuery);
//        when(jpaQuery.fetch()).thenReturn(expectedRideDtos);
//        when(jpaQuery.fetchCount()).thenReturn(expectedTotal);
//
//        // Act
//        Page<RideHistoryDto> result = rideService.getDriverRideHistory(testDriverUuid, searchCriteria, pageRequest);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(expectedRideDtos.size(), result.getContent().size());
//        assertEquals(expectedRideDtos, result.getContent());
//        assertEquals(expectedTotal, result.getTotalElements());
//        assertEquals(pageRequest.getPageNumber(), result.getNumber());
//        assertEquals(pageRequest.getPageSize(), result.getSize());
//
//        // Verify QueryDSL interactions
//        verify(queryFactory).select(any(Expression.class)); // Check if select was called
//        verify(jpaQuery).from(QRide.ride);
//        verify(jpaQuery).where(any(Predicate.class));
//        verify(jpaQuery).offset(pageRequest.getOffset());
//        verify(jpaQuery).limit(pageRequest.getPageSize());
//        verify(jpaQuery).fetch();
//        // fetchCount() is called internally by query.fetch() in some QueryDSL versions,
//        // or explicitly before/after. Verify it was called at least once.
//        verify(jpaQuery, atLeastOnce()).fetchCount();
//
//
//        // Verify predicate: Should only contain the driverUuid condition
//        BooleanBuilder expectedPredicate = new BooleanBuilder();
//        expectedPredicate.and(QRide.ride.driverUuid.eq(testDriverUuid));
//        // Comparing BooleanBuilder instances directly is complex.
//        // It's often better to verify that the *correct* criteria methods were called
//        // or inspect the captured predicate's string representation if feasible.
//        // For simplicity here, we trust the `where` was called. A more robust test
//        // might involve asserting specific parts of the captured predicate.
//        assertNotNull(predicateCaptor.getValue()); // Basic check
//        // Example of string check (can be brittle):
//        assertTrue(predicateCaptor.getValue().toString().contains("ride.driverUuid = " + testDriverUuid));
//
//        // Verify pagination
//        assertEquals(pageRequest.getOffset(), offsetCaptor.getValue());
//        assertEquals(pageRequest.getPageSize(), limitCaptor.getValue());
//    }
//
//}
