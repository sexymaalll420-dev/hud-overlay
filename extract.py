#!/usr/bin/env python3
import re
from pathlib import Path

src = Path('/root/hud_app/123_.txt')
text = src.read_text(encoding='utf-8', errors='replace')

# Find patterns: cat > <path> <<'XEOF'
pattern = re.compile(r"cat > ([^\n]+?) <<'XEOF'\n")
for m in pattern.finditer(text):
    rel = m.group(1).strip()
    start = m.end()
    end = text.find("\nXEOF\n", start)
    if end < 0:
        end = text.find("\nXEOF\r\n", start)
    if end < 0:
        # try end of file style
        end = text.find("\nXEOF", start)
    if end < 0:
        continue
    content = text[start:end]
    out = Path('/root/hud_app') / rel
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(content, encoding='utf-8')
    print('wrote', rel)
