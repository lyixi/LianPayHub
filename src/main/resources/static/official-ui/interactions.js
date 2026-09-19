(() => {
  const themeToggle = document.querySelector("[data-theme-toggle]");
  const setTheme = theme => {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem("lyixi-theme", theme);
    themeToggle?.setAttribute("aria-label", theme === "dark" ? "切换到浅色主题" : "切换到深色主题");
    document.querySelector('meta[name="theme-color"]')?.setAttribute("content", theme === "dark" ? "#0a0a0b" : "#faf9f6");
  };
  themeToggle?.addEventListener("click", () => setTheme(document.documentElement.dataset.theme === "dark" ? "light" : "dark"));
  setTheme(document.documentElement.dataset.theme || "light");
  const reduceMotion = matchMedia("(prefers-reduced-motion: reduce)").matches;
  const finePointer = matchMedia("(pointer: fine)").matches;
  const header = document.querySelector(".site-header");
  const progress = document.querySelector(".scroll-progress i");
  let scrollTick = false;

  function updateScrollState() {
    const max = Math.max(1, document.documentElement.scrollHeight - innerHeight);
    const ratio = Math.min(1, scrollY / max);
    progress.style.transform = `scaleX(${ratio})`;
    header?.classList.toggle("is-compact", scrollY > 36);
    scrollTick = false;
  }

  addEventListener("scroll", () => {
    if (!scrollTick) {
      scrollTick = true;
      requestAnimationFrame(updateScrollState);
    }
  }, { passive: true });
  updateScrollState();

  function addPressFeedback(node) {
    if (node.dataset.feedbackReady) return;
    node.dataset.feedbackReady = "true";
    node.addEventListener("pointerdown", () => node.classList.add("is-pressed"));
    ["pointerup", "pointercancel", "pointerleave"].forEach(type => {
      node.addEventListener(type, () => node.classList.remove("is-pressed"));
    });
  }

  document.querySelectorAll("a, button").forEach(addPressFeedback);

  if (finePointer && !reduceMotion) {
    document.querySelectorAll(".button, .header-action").forEach(node => {
      node.addEventListener("pointermove", event => {
        const rect = node.getBoundingClientRect();
        const x = event.clientX - rect.left - rect.width / 2;
        const y = event.clientY - rect.top - rect.height / 2;
        node.style.transform = `translate(${x * .1}px, ${y * .12}px)`;
      });
      node.addEventListener("pointerleave", () => { node.style.transform = ""; });
    });
  }

  const stage = document.querySelector("#productStage");
  if (stage && finePointer && !reduceMotion) {
    let targetX = 0, targetY = 0, currentX = 0, currentY = 0, active = false;
    stage.addEventListener("pointermove", event => {
      const rect = stage.getBoundingClientRect();
      const px = (event.clientX - rect.left) / rect.width;
      const py = (event.clientY - rect.top) / rect.height;
      targetX = (py - .5) * -5;
      targetY = (px - .5) * 7;
      active = true;
    });
    stage.addEventListener("pointerleave", () => { targetX = 0; targetY = 0; active = false; });
    const animateTilt = () => {
      currentX += (targetX - currentX) * .09;
      currentY += (targetY - currentY) * .09;
      stage.style.setProperty("--rx", `${currentX.toFixed(2)}deg`);
      stage.style.setProperty("--ry", `${currentY.toFixed(2)}deg`);
      if (active || Math.abs(currentX) > .01 || Math.abs(currentY) > .01) requestAnimationFrame(animateTilt);
    };
    stage.addEventListener("pointerenter", () => requestAnimationFrame(animateTilt), { once: false });
  }

  function prepareInteractiveCards(root = document) {
    root.querySelectorAll(".product-row, .journal-card, .principles > div").forEach(card => {
      if (card.dataset.cardReady) return;
      card.dataset.cardReady = "true";
      addPressFeedback(card);
      if (finePointer && !reduceMotion) {
        card.addEventListener("pointermove", event => {
          const rect = card.getBoundingClientRect();
          card.style.setProperty("--spot-x", `${event.clientX - rect.left}px`);
          card.style.setProperty("--spot-y", `${event.clientY - rect.top}px`);
        });
      }
    });
  }

  prepareInteractiveCards();
  const productList = document.querySelector("#productsGrid");
  const storiesList = document.querySelector("#storiesList");
  [productList, storiesList].filter(Boolean).forEach(node => {
    new MutationObserver(() => prepareInteractiveCards(node)).observe(node, { childList: true });
  });

  document.querySelectorAll("[data-chapter]").forEach(button => {
    button.addEventListener("click", () => {
      document.querySelectorAll("[data-chapter]").forEach(item => item.classList.toggle("active", item === button));
      const title = document.querySelector(".editor-sheet h2");
      const excerpt = document.querySelector(".editor-sheet .preview-excerpt");
      const chapterNo = document.querySelector(".editor-sheet > small");
      const selectedTitle = button.dataset.previewTitle || button.querySelector("b")?.textContent;
      const selectedExcerpt = button.dataset.previewExcerpt;
      if (!selectedTitle || !title || !excerpt) return;
      title.animate([{ opacity: .25, transform: "translateY(5px)" }, { opacity: 1, transform: "none" }], { duration: 260, easing: "ease-out" });
      title.textContent = selectedTitle;
      chapterNo.textContent = `第${button.dataset.chapter}章`;
      if (selectedExcerpt) excerpt.textContent = selectedExcerpt;
    });
  });

  const analysisButton = document.querySelector(".analysis-trigger");
  if (analysisButton) {
    analysisButton.addEventListener("click", () => {
      if (analysisButton.dataset.running === "true") return;
      analysisButton.dataset.running = "true";
      const original = analysisButton.innerHTML;
      analysisButton.innerHTML = "正在分析上下文 <span>•••</span>";
      analysisButton.classList.add("is-loading");
      setTimeout(() => {
        analysisButton.innerHTML = "分析已更新 <span>✓</span>";
        analysisButton.classList.remove("is-loading");
        analysisButton.classList.add("is-success");
        setTimeout(() => {
          analysisButton.innerHTML = original;
          analysisButton.classList.remove("is-success");
          analysisButton.dataset.running = "false";
        }, 1800);
      }, 900);
    });
  }

  const canvas = document.querySelector("#signalCanvas");
  if (canvas && !reduceMotion) {
    const context = canvas.getContext("2d");
    const points = Array.from({ length: 22 }, (_, index) => ({
      x: ((index * 47) % 100) / 100,
      y: ((index * 71) % 100) / 100,
      phase: index * .47
    }));
    let width = 0, height = 0, ratio = 1, frame = 0;
    const resize = () => {
      const rect = canvas.getBoundingClientRect();
      ratio = Math.min(devicePixelRatio || 1, 2);
      width = rect.width;
      height = rect.height;
      canvas.width = Math.round(width * ratio);
      canvas.height = Math.round(height * ratio);
      context.setTransform(ratio, 0, 0, ratio, 0, 0);
    };
    const draw = time => {
      context.clearRect(0, 0, width, height);
      const nodes = points.map(point => ({
        x: point.x * width + Math.sin(time * .00035 + point.phase) * 7,
        y: point.y * height + Math.cos(time * .00028 + point.phase) * 6
      }));
      context.lineWidth = 1;
      for (let i = 0; i < nodes.length; i += 1) {
        for (let j = i + 1; j < nodes.length; j += 1) {
          const dx = nodes[i].x - nodes[j].x;
          const dy = nodes[i].y - nodes[j].y;
          const distance = Math.hypot(dx, dy);
          if (distance < 115) {
            const alpha = (1 - distance / 115) * .16;
            context.strokeStyle = document.documentElement.dataset.theme === "dark" ? `rgba(200,169,110,${alpha})` : `rgba(154,123,63,${alpha})`;
            context.beginPath();
            context.moveTo(nodes[i].x, nodes[i].y);
            context.lineTo(nodes[j].x, nodes[j].y);
            context.stroke();
          }
        }
        context.fillStyle = i % 5 === 0 ? "rgba(200,169,110,.55)" : "rgba(128,124,116,.28)";
        context.fillRect(nodes[i].x - 1, nodes[i].y - 1, 2, 2);
      }
      frame = requestAnimationFrame(draw);
    };
    const observer = new IntersectionObserver(entries => {
      if (entries[0].isIntersecting && !frame) frame = requestAnimationFrame(draw);
      if (!entries[0].isIntersecting && frame) { cancelAnimationFrame(frame); frame = 0; }
    });
    resize();
    addEventListener("resize", resize, { passive: true });
    observer.observe(canvas);
  }
})();
