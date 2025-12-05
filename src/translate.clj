(ns translate
  (:require [files]
            [blog]
            [clj-yaml.core :as yaml]
            [cuerdas.core :as cstr]
            [ai]))

(comment
;; intro text cleanup
  (doseq [file (->> (files/list-dir-files "content/post")
                    (drop 79)
                    (take 1))]
    (let [{:keys [markdown metadata]} (blog/parse-md-file file)
          md-file-path (:md-path metadata)
          frontmatter (str "---\n"
                           (yaml/generate-string
                            (-> metadata
                                (dissoc :md-path))
                            :dumper-options {:flow-style :block})
                           "---\n\n")
          md-no-header (cstr/replace markdown frontmatter "")
          cleaner-prompt
          "You are an expert HTML cleaner and text editor. 
Your task is to process a block of HTML text to remove all unnecessary and invisible tags while strictly preserving the text content and all **existing anchor tags (<a>)**, including their **href attributes** 
and the **link text** inside them.

**Instructions:**
1.  **Remove** all unrendered HTML tags except existing `<a>` (anchor) tags. Specifically, remove tags like `<p>`, `<span>`, `<div>` etc., and their corresponding closing tags.
2.  **Do NOT** translate or alter the Ukrainian text.
3.  **Crucially, do NOT create new <a> tags.** Only keep the ones present in the input.
4.  **Preserve** the text content inside visible tags like `<b>`, `<i>`, `<strong>`.
5.  **Response ONLY with result** without any explanations.

**Input HTML Block:**"
          cleaned-introtext (ai/ask (str
                                     cleaner-prompt "\n" (:introtext metadata)))
          frontmatter-updated (str "---\n"
                                   (yaml/generate-string
                                    (-> metadata
                                        (dissoc :md-path)
                                        (assoc :introtext cleaned-introtext))
                                    :dumper-options {:flow-style :block})
                                   "---\n\n")

          file-path (str "content/posts-en/" (:alias metadata) ".md")]
      (files/make-parents-dirs  file-path)
      (spit md-file-path (str frontmatter-updated md-no-header))))

;;
  )

(comment
  (->> (files/list-dir-files "content/post")
       (map-indexed #(hash-map :idx %1 :file %2))
       (filter #(cstr/includes? (:file %) "yohurty-zhyvi-chy-ne-duzhe"))))

(comment
  ;; translate to english
  (doseq [file (->> (files/list-dir-files "content/post")
                    (drop 70)
                    (take 10))]

    (let [#_#_file (->> (files/list-dir-files "content/post")
                        (drop 8)
                        (take 1)
                        first)
          {:keys [markdown metadata]} (blog/parse-md-file file)
          frontmatter (str "---\n"
                           (yaml/generate-string
                            (-> metadata
                                (dissoc :md-path))
                            :dumper-options {:flow-style :block})
                           "---\n\n")
          md-no-header (cstr/replace markdown frontmatter "")
          translate-content-prompt "You are a professional translator that translates Markdown documents from Ukrainian to English.

Your task:
- Input is a Markdown file that contains article text.
- Translate **all visible Ukrainian text** into **natural, fluent English**, keeping tone and meaning.
- Preserve: Markdown structure, headings, bold/italic formatting, links, and image references.
- Do **not** translate URLs, file paths, date formats, or field names.
- The output must remain **valid Markdown**.

Output only the translated Markdown document — no commentary, no extra explanations.

Here is the input:\n\n"
          translate-intro-prompt "You are a professional translator that translates Markdown documents from Ukrainian to English.

Your task:
- Input is a Markdown summary text to the article.
- Translate **all visible Ukrainian text** into **natural, fluent English**, keeping tone and meaning.
- Preserve: Markdown structure, headings, bold/italic formatting, links, and image references.
- Do **not** translate URLs, file paths, date formats, or field names.
- The output must remain **valid Markdown** or HTML.

Output only the translated text — no commentary, no extra explanations.

Here is the input:\n\n"
          translate-title-prompt "You are a professional translator that translates Markdown documents from Ukrainian to English.

Your task:
- Input is a title to the article.
- Translate **all visible Ukrainian text** into **natural, fluent English**, keeping tone and meaning.
- Preserve: Markdown structure, headings, bold/italic formatting, links, and image references.
- Do **not** translate URLs, file paths, date formats, or field names.
- The output must remain **valid Markdown** or HTML.
- There is also a summary of the article, so that you can adapt your translation if needed.

Output only the translated title — no commentary, no extra explanations. Do **not** include any summary text in the output.

Here is the title to translate:\n\n"
          translated-intro (ai/ask (str
                                    translate-intro-prompt "\n" (:introtext metadata)))
          translated-title (ai/ask (str
                                    translate-title-prompt "\n" (:title metadata)
                                    "\n\n Here is summary of the text for the context: \n\n" translated-intro))
          translated-content (ai/ask (str
                                      translate-content-prompt "\n" md-no-header))
          translated-alias (cstr/slug translated-title)
          translated-frontmatter (str "---\n"
                                      (yaml/generate-string
                                       (-> metadata
                                           (dissoc :md-path :keywords :description)
                                           (assoc :title translated-title
                                                  :introtext translated-intro
                                                  :alias translated-alias))
                                       :dumper-options {:flow-style :block})
                                      "---\n\n")

          file-path (str "content/posts-en/" translated-alias ".md")]
      (files/make-parents-dirs  file-path)
      (spit file-path (str translated-frontmatter translated-content)))

    ;;
    ))


