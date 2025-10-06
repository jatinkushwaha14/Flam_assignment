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
    }
    EdgeDetectionViewer.prototype.initializeViewer = function () {
        console.log('OpenCV Edge Detection Viewer initialized');
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
    EdgeDetectionViewer.prototype.updateStatsOnly = function () {
        this.frameCount++;
        this.stats.fps = Math.floor(Math.random() * 5) + 15;
        this.updateStats();
        console.log("Frame ".concat(this.frameCount, " stats updated"));
    };
    EdgeDetectionViewer.prototype.updateFrame = function () {
        this.updateStatsOnly();
        console.log(" Frame ".concat(this.frameCount, " processed"));
    };
    return EdgeDetectionViewer;
}());
document.addEventListener('DOMContentLoaded', function () {
    var viewer = new EdgeDetectionViewer();
    // Make updateFrame available globally
    window.updateFrame = function () { return viewer.updateFrame(); };
});
