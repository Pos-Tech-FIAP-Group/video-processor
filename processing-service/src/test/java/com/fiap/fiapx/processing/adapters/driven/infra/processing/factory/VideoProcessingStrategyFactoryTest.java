package com.fiap.fiapx.processing.adapters.driven.infra.processing.factory;

import com.fiap.fiapx.processing.core.application.ports.VideoProcessingStrategyPort;
import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VideoProcessingStrategyFactoryTest {
    
    @Mock
    private VideoProcessingStrategyPort mp4Strategy;
    
    @Mock
    private VideoProcessingStrategyPort aviStrategy;
    
    private VideoProcessingStrategyFactory factory;
    
    @BeforeEach
    void setUp() {
        when(mp4Strategy.supports(VideoFormat.MP4)).thenReturn(true);
        when(mp4Strategy.supports(VideoFormat.AVI)).thenReturn(false);
        when(aviStrategy.supports(VideoFormat.AVI)).thenReturn(true);
        when(aviStrategy.supports(VideoFormat.MP4)).thenReturn(false);
        
        List<VideoProcessingStrategyPort> strategies = Arrays.asList(mp4Strategy, aviStrategy);
        factory = new VideoProcessingStrategyFactory(strategies);
    }
    
    @Test
    void shouldReturnMp4StrategyForMp4Format() {
        // When
        VideoProcessingStrategyPort result = factory.getStrategy(VideoFormat.MP4);
        
        // Then
        assertEquals(mp4Strategy, result);
    }
    
    @Test
    void shouldReturnAviStrategyForAviFormat() {
        // When
        VideoProcessingStrategyPort result = factory.getStrategy(VideoFormat.AVI);
        
        // Then
        assertEquals(aviStrategy, result);
    }
    
    @Test
    void shouldThrowExceptionWhenStrategyNotFound() {
        // When/Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> factory.getStrategy(VideoFormat.MOV)
        );
        
        assertTrue(exception.getMessage().contains("No strategy found"));
    }
    
    @Test
    void shouldReturnEmptyWhenFormatIsNull() {
        // When
        var result = factory.findStrategy(null);
        
        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindStrategyWhenFormatIsSupported() {
        // When
        var result = factory.findStrategy(VideoFormat.MP4);

        // Then
        assertTrue(result.isPresent());
        assertEquals(mp4Strategy, result.get());
    }
}
