# CLW Production Manager

Windows desktop application for local CLW1/CLW2 production data entry and analysis.

## Build
GitHub Actions builds a standalone Windows EXE on every relevant push and on manual dispatch.
Open the Actions tab and download artifact **CLW_Production_Manager_Windows** from the successful run.
The EXE is portable; Python is not required on the target PC.

## Local data
Data are stored in %LOCALAPPDATA%\\CLWProductionManager\\data.json.
Back up this file to preserve records.

## Features
- Separate CLW1 and CLW2 screens: data table, graphs, add data.
- Sequential data entry; skipped quantity defaults to zero.
- Date picker, shift and TL selection.
- Editable/deletable records.
- Settings for TLs and production/defect items.
- Daily, monthly, yearly and custom date-range graph filters.
- Shift/TL filters, KS percentage and defect loss (48 seconds per defect).

The first configured item on each line is treated as KS for percentage calculations.
