var EdgeDetectionViewer = /** @class */ (function () {
    function EdgeDetectionViewer() {
        this.frameCount = 0;
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
    EdgeDetectionViewer.prototype.initializeViewer = function () {
        console.log('🎯 OpenCV Edge Detection Viewer initialized');
        this.updateStats();
    };
    EdgeDetectionViewer.prototype.updateStats = function () {
        var resolutionEl = document.getElementById('resolution');
        var fpsEl = document.getElementById('fps');
        var processingEl = document.getElementById('processing');
        if (resolutionEl)
            resolutionEl.textContent = "".concat(this.stats.width, "x").concat(this.stats.height);
        if (fpsEl)
            fpsEl.textContent = "".concat(this.stats.fps, " FPS");
        if (processingEl)
            processingEl.textContent = this.stats.processing;
    };
    EdgeDetectionViewer.prototype.simulateFrameProcessing = function () {
        this.frameCount++;
        var frameDisplay = document.getElementById('frameDisplay');
        if (frameDisplay) {
            var patterns = [
                'Edge Detection: Building Outlines',
                'Edge Detection: Hand Gestures',
                'Edge Detection: Object Boundaries',
                'Edge Detection: Face Contours'
            ];
            var currentPattern = patterns[this.frameCount % patterns.length];
            frameDisplay.innerHTML = "\n                <strong>Frame #".concat(this.frameCount, "</strong><br>\n                ").concat(currentPattern, "<br>\n                <small>Processed with OpenCV C++</small>\n            ");
            this.stats.fps = Math.floor(Math.random() * 5) + 15; // 15-20 FPS
            this.updateStats();
        }
    };
    EdgeDetectionViewer.prototype.startFrameSimulation = function () {
        var _this = this;
        setInterval(function () {
            _this.simulateFrameProcessing();
        }, 100); // 10 FPS simulation
    };
    EdgeDetectionViewer.prototype.updateFrame = function () {
        this.simulateFrameProcessing();
        console.log("\uD83D\uDCF1 Frame ".concat(this.frameCount, " processed"));
    };
    return EdgeDetectionViewer;
}());
document.addEventListener('DOMContentLoaded', function () {
    var viewer = new EdgeDetectionViewer();
    // Make updateFrame available globally
    window.updateFrame = function () { return viewer.updateFrame(); };
});
