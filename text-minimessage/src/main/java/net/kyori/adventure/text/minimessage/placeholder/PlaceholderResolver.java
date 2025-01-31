/*
 * This file is part of adventure, licensed under the MIT License.
 *
 * Copyright (c) 2017-2022 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.kyori.adventure.text.minimessage.placeholder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * A resolver for user-defined placeholders.
 *
 * @since 4.10.0
 */
@FunctionalInterface
public interface PlaceholderResolver {
  /**
   * Create a new builder for a {@link PlaceholderResolver}.
   *
   * @return a new builder
   * @since 4.10.0
   */
  static @NotNull Builder builder() {
    return new PlaceholderResolverBuilderImpl();
  }

  /**
   * Constructs a placeholder resolver from a map.
   *
   * <p>The returned placeholder resolver will make a copy of the provided map.
   * This means that changes to the map will not be reflected in the placeholder resolver.</p>
   *
   * @param map the map
   * @return the placeholder resolver
   * @since 4.10.0
   */
  static @NotNull PlaceholderResolver map(final @NotNull Map<String, Replacement<?>> map) {
    return new MapPlaceholderResolver(Objects.requireNonNull(map, "map"));
  }

  /**
   * Constructs a placeholder resolver from some placeholders.
   *
   * @param placeholders the placeholders
   * @return the placeholder resolver
   * @since 4.10.0
   */
  static @NotNull PlaceholderResolver placeholders(final @NotNull Placeholder<?> @NotNull ... placeholders) {
    if (Objects.requireNonNull(placeholders, "placeholders").length == 0) return empty();
    return placeholders(Arrays.asList(placeholders));
  }

  /**
   * Constructs a placeholder resolver from some placeholders.
   *
   * @param placeholders the placeholders
   * @return the placeholder resolver
   * @since 4.10.0
   */
  static @NotNull PlaceholderResolver placeholders(final @NotNull Iterable<? extends Placeholder<?>> placeholders) {
    final Map<String, Placeholder<?>> placeholderMap = new HashMap<>();

    for (final Placeholder<?> placeholder : Objects.requireNonNull(placeholders, "placeholders")) {
      Objects.requireNonNull(placeholder, "placeholders must not contain null elements");
      placeholderMap.put(placeholder.key(), placeholder);
    }

    if (placeholderMap.isEmpty()) return empty();

    return new MapPlaceholderResolver(placeholderMap);
  }

  /**
   * Constructs a placeholder resolver capable of resolving from multiple sources.
   *
   * @param resolvers the placeholder resolvers
   * @return the placeholder resolver
   * @since 4.10.0
   */
  static @NotNull PlaceholderResolver combining(final @NotNull PlaceholderResolver@NotNull... resolvers) {
    if (Objects.requireNonNull(resolvers, "resolvers").length == 1) {
      return Objects.requireNonNull(resolvers[0], "resolvers must not contain null elements");
    }
    return combining(Arrays.asList(resolvers));
  }

  /**
   * Constructs a placeholder resolver capable of resolving from multiple sources, in iteration order.
   *
   * <p>The provided iterable is copied. This means changes to the iterable will not reflect in the returned resolver.</p>
   *
   * @param resolvers the placeholder resolvers
   * @return the placeholder resolver
   * @since 4.10.0
   */
  static @NotNull PlaceholderResolver combining(final @NotNull Iterable<? extends PlaceholderResolver> resolvers) {
    final List<PlaceholderResolver> copiedResolvers = resolvers instanceof Collection<?> ? new ArrayList<>(((Collection<?>) resolvers).size()) : new ArrayList<>();

    for (final PlaceholderResolver resolver : Objects.requireNonNull(resolvers, "resolvers")) {
      copiedResolvers.add(Objects.requireNonNull(resolver, "resolvers cannot contain null elements"));
    }

    final int size = copiedResolvers.size();
    if (size == 0) return empty();
    if (size == 1) return copiedResolvers.get(0);
    return new GroupedPlaceholderResolver(copiedResolvers);
  }

  /**
   * Constructs a placeholder resolver capable of dynamically resolving placeholders.
   *
   * <p>The resolver can return {@code null} to indicate it cannot resolve a placeholder.
   * Once a string to replacement mapping has been created, it will be cached to avoid
   * the cost of recreating the replacement.</p>
   *
   * @param resolver the resolver
   * @return the placeholder resolver
   * @since 4.10.0
   */
  static @NotNull PlaceholderResolver dynamic(final @NotNull Function<String, @Nullable Replacement<?>> resolver) {
    return new DynamicPlaceholderResolver(Objects.requireNonNull(resolver, "resolver"));
  }

  /**
   * An empty placeholder resolver that will return {@code null} for all resolve attempts.
   *
   * @return the placeholder resolver
   * @since 4.10.0
   */
  static @NotNull PlaceholderResolver empty() {
    return EmptyPlaceholderResolver.INSTANCE;
  }

  /**
   * Returns the replacement for a given key, if any exist.
   *
   * <p>This method might be called multiple times during each parse attempt. This is due to the
   * fact that it is used in places to check if a tag is a placeholder or not. Therefore, you
   * should prefer using fixed or cached replacements instead of dynamic construction.</p>
   *
   * @param key the key
   * @return the replacement
   * @since 4.10.0
   */
  @Nullable Replacement<?> resolve(final @NotNull String key);

  /**
   * A builder to gradually construct placeholder resolvers.
   *
   * <p>Entries added later will take priority over entries added earlier.</p>
   *
   * @since 4.10.0
   */
  interface Builder {
    /**
     * Add a single placeholder to this resolver.
     *
     * @param placeholder the placeholder
     * @return this builder
     * @since 4.10.0
     */
    @NotNull Builder placeholder(final @NotNull Placeholder<?> placeholder);

    /**
     * Add placeholders to this resolver.
     *
     * @param placeholders placeholders to add
     * @return this builder
     * @since 4.10.0
     */
    default @NotNull Builder placeholders(final @NotNull Placeholder<?> @NotNull... placeholders) {
      return this.placeholders(Arrays.asList(requireNonNull(placeholders, "placeholders")));
    }

    /**
     * Add placeholders to this resolver.
     *
     * @param placeholders placeholders to add
     * @return this builder
     * @since 4.10.0
     */
    @NotNull Builder placeholders(final @NotNull Iterable<Placeholder<?>> placeholders);

    /**
     * Add placeholders to this resolver.
     *
     * <p>A snapshot of the map will be added to this resolver, rather than a live view.</p>
     *
     * @param replacements placeholders to add
     * @return this builder
     * @since 4.10.0
     */
    @NotNull Builder placeholders(final @NotNull Map<String, Replacement<?>> replacements);

    /**
     * Add a placeholder resolver to those queried by the result of this builder.
     *
     * @param resolver the resolver to add
     * @return this builder
     * @since 4.10.0
     */
    @NotNull Builder resolver(final @NotNull PlaceholderResolver resolver);

    /**
     * Add placeholder resolvers to those queried by the result of this builder.
     *
     * @param resolvers the resolvers to add
     * @return this builder
     * @since 4.10.0
     */
    @NotNull Builder resolvers(final @NotNull PlaceholderResolver@NotNull... resolvers);

    /**
     * Add placeholder resolvers to those queried by the result of this builder.
     *
     * @param resolvers the resolvers to add
     * @return this builder
     * @since 4.10.0
     */
    @NotNull Builder resolvers(final @NotNull Iterable<? extends PlaceholderResolver> resolvers);

    /**
     * Add a resolver that dynamically queries and caches based on the provided function.
     *
     * @param dynamic the function to query for replacements
     * @return this builder
     * @since 4.10.0
     */
    default @NotNull Builder dynamic(final @NotNull Function<String, @Nullable Replacement<?>> dynamic) {
      return this.resolver(PlaceholderResolver.dynamic(dynamic));
    }

    /**
     * Create a placeholder resolver based on the input.
     *
     * <p>If no elements are added, this may return an empty resolver.</p>
     *
     * @return the resolver
     * @since 4.10.0
     */
    @NotNull PlaceholderResolver build();
  }
}
