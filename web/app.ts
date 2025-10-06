// TypeScript Web Viewer for OpenCV Edge Detection
interface FrameStats {
    width: number;
    height: number;
    fps: number;
    processing: string;
    timestamp: number;
}

class EdgeDetectionViewer {
    private stats: FrameStats;
    private frameCount: number = 0;
    
    constructor() {
        this.stats = {
            width: 1280,
            height: 720,
            fps: 18,
            processing: 'Canny Edge Detection',
            timestamp: Date.now()
        };
        
        this.initializeViewer();
        this.startFrameSimulation();
    }
    
    private initializeViewer(): void {
        console.log('🎯 OpenCV Edge Detection Viewer initialized');
        this.updateStats();
    }
    
    private updateStats(): void {
        const resolutionEl = document.getElementById('resolution');
        const fpsEl = document.getElementById('fps');
        const processingEl = document.getElementById('processing');
        
        if (resolutionEl) resolutionEl.textContent = `${this.stats.width}x${this.stats.height}`;
        if (fpsEl) fpsEl.textContent = `${this.stats.fps} FPS`;
        if (processingEl) processingEl.textContent = this.stats.processing;
    }
    
    private simulateFrameProcessing(): void {
        this.frameCount++;
        const frameDisplay = document.getElementById('frameDisplay');
        
        if (frameDisplay) {

            const patterns = [
                'Edge Detection: Building Outlines',
                'Edge Detection: Hand Gestures', 
                'Edge Detection: Object Boundaries',
                'Edge Detection: Face Contours'
            ];
            
            const currentPattern = patterns[this.frameCount % patterns.length];
            frameDisplay.innerHTML = `
                <strong>Frame #${this.frameCount}</strong><br>
                ${currentPattern}<br>
                <small>Processed with OpenCV C++</small>
            `;
            

            this.stats.fps = Math.floor(Math.random() * 5) + 15; // 15-20 FPS
            this.updateStats();
        }
    }
    
    private startFrameSimulation(): void {

        setInterval(() => {
            this.simulateFrameProcessing();
        }, 100); // 10 FPS simulation
    }
    
    public updateFrame(): void {
        this.simulateFrameProcessing();
        console.log(`📱 Frame ${this.frameCount} processed`);
    }
}


document.addEventListener('DOMContentLoaded', () => {
    const viewer = new EdgeDetectionViewer();
    
    // Make updateFrame available globally
    (window as any).updateFrame = () => viewer.updateFrame();
});


