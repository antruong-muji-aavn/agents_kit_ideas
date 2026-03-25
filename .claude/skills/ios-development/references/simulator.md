# iOS Simulator Control

List, boot, shutdown, and manage iOS simulators using XcodeBuildMCP or xcrun simctl.

## Usage

```
simulator --list                    # List available simulators
simulator --boot "iPhone 16 Pro"    # Boot simulator
simulator --shutdown                # Shutdown booted simulator
simulator --install MyApp.app       # Install app
simulator --launch com.myapp.bundle # Launch app
simulator --screenshot              # Take screenshot
```

## Process

1. **Parse action** from user request
2. **Use MCP tools** if XcodeBuildMCP is available — preferred
3. **Fallback** to `xcrun simctl` via Bash if MCP unavailable

## MCP Tool Usage (preferred)

```swift
// List simulators
mcp__xcodebuildmcp__list_sims()

// Boot
mcp__xcodebuildmcp__boot_sim({ simulatorId: 'UUID' })
mcp__xcodebuildmcp__open_sim({ simulatorId: 'UUID' })

// Install
mcp__xcodebuildmcp__install_app_sim({ simulatorId: 'UUID', appPath: '/path/to/MyApp.app' })

// Launch
mcp__xcodebuildmcp__launch_app_sim({ simulatorId: 'UUID', bundleId: 'com.myapp.bundle' })

// Stop
mcp__xcodebuildmcp__stop_app_sim({ simulatorId: 'UUID', bundleId: 'com.myapp.bundle' })

// Screenshot
mcp__xcodebuildmcp__screenshot({ simulatorId: 'UUID', outputPath: '/path/to/screenshot.png' })
```

## Fallback Commands (xcrun simctl)

```bash
xcrun simctl list devices available          # List
xcrun simctl boot "iPhone 16 Pro"            # Boot
open -a Simulator
xcrun simctl shutdown booted                 # Shutdown
xcrun simctl install booted MyApp.app        # Install
xcrun simctl launch booted com.myapp.bundle  # Launch
xcrun simctl io booted screenshot out.png    # Screenshot
```

## Rules

- Always run `list_sims` first to get device UDIDs before booting
- Shut down simulators when done to free resources
- Use simulator for iteration, physical device for final validation
